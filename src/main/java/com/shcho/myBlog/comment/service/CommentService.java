package com.shcho.myBlog.comment.service;

import com.shcho.myBlog.comment.dto.CommentDeleteRequestDto;
import com.shcho.myBlog.comment.dto.CommentCreateRequestDto;
import com.shcho.myBlog.comment.dto.CommentUpdateRequestDto;
import com.shcho.myBlog.comment.dto.CommentWithRepliesDto;
import com.shcho.myBlog.comment.dto.ReplyResponseDto;
import com.shcho.myBlog.comment.entity.Comment;
import com.shcho.myBlog.comment.repository.CommentRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.libs.exception.ErrorCode;
import com.shcho.myBlog.post.entity.Post;
import com.shcho.myBlog.post.repository.PostRepository;
import com.shcho.myBlog.user.entity.Role;
import com.shcho.myBlog.user.entity.User;
import com.shcho.myBlog.user.repository.UserRepository;
import com.shcho.myBlog.user.service.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Comment createComment(CustomUserDetails userDetails, Long postId, CommentCreateRequestDto request) {
        Post post = postRepository.getReferenceById(postId);

        if (userDetails != null) {
            User user = userRepository.getReferenceById(userDetails.getUserId());
            return commentRepository.save(
                    Comment.of(request.content(), user, post)
            );
        } else {
            String rawPassword = request.anonymousPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User guest = User.builder()
                    .username("guest-" + UUID.randomUUID())
                    .password(encodedPassword)
                    .nickname(request.anonymousName())
                    .role(Role.GUEST)
                    .build();
            userRepository.save(guest);

            return commentRepository.save(
                    Comment.of(request.content(), guest, post)
            );
        }
    }

    public Page<CommentWithRepliesDto> getCommentsWithReplies(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId, pageable);
        return comments.map(comment -> {
            List<ReplyResponseDto> repliesDto = comment.getReplies().stream()
                    .map(ReplyResponseDto::from)
                    .toList();
            return CommentWithRepliesDto.from(comment, repliesDto);
        });
    }

    @Transactional
    public Comment updateComment(CustomUserDetails userDetails, Long commentId, CommentUpdateRequestDto request) {
        Comment comment = getCommentById(commentId);

        checkWriter(comment, userDetails, request.anonymousPassword());

        comment.update(request.content());
        return comment;
    }

    @Transactional
    public void deleteComment(CustomUserDetails userDetails, Long commentId, CommentDeleteRequestDto request) {
        Comment comment = getCommentById(commentId);

        checkWriter(comment, userDetails, request.anonymousPassword());

        comment.delete();
    }

    private Comment getCommentById(Long commentId) {
        Comment comment = commentRepository.getReferenceById(commentId);

        if (comment.isDeleted()) {
            throw new CustomException(ErrorCode.ALREADY_DELETED_COMMENT);
        }

        return comment;
    }

    private void checkWriter(Comment comment, CustomUserDetails userDetails, String anonymousPassword) {
        if (userDetails != null) {
            User user = userRepository.getReferenceById(userDetails.getUserId());

            if (!user.getId().equals(comment.getUser().getId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
            }
        } else {
            if (anonymousPassword == null || !passwordEncoder.matches(anonymousPassword, comment.getUser().getPassword())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
            }
        }
    }
}
