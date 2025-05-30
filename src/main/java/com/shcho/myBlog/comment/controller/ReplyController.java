package com.shcho.myBlog.comment.controller;

import com.shcho.myBlog.comment.dto.ReplyCreateRequestDto;
import com.shcho.myBlog.comment.dto.ReplyDeleteRequestDto;
import com.shcho.myBlog.comment.dto.ReplyResponseDto;
import com.shcho.myBlog.comment.dto.ReplyUpdateRequestDto;
import com.shcho.myBlog.comment.entity.Reply;
import com.shcho.myBlog.comment.service.ReplyService;
import com.shcho.myBlog.user.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments/{commentId}/replies")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    @PostMapping
    public ResponseEntity<ReplyResponseDto> createReply (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody @Valid ReplyCreateRequestDto request
            ) {
        Reply reply = replyService.createReply (userDetails, commentId, request);
        return ResponseEntity.ok(ReplyResponseDto.from(reply));
    }

    @PatchMapping("/{replyId}")
    public ResponseEntity<ReplyResponseDto> updateReply (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @RequestBody @Valid ReplyUpdateRequestDto request
    ) {
        Reply updatedReply = replyService.updateReply(replyId, userDetails, request);
        return ResponseEntity.ok(ReplyResponseDto.from(updatedReply));
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<ReplyResponseDto> deleteReply (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @RequestBody @Valid ReplyDeleteRequestDto request
    ) {
        replyService.deleteReply(replyId, userDetails, request);
        return ResponseEntity.noContent().build();
    }
}
