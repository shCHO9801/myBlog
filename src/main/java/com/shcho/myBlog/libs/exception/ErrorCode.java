package com.shcho.myBlog.libs.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* 404 NOT_FOUND */
    USER_NOT_FOUND(404, "유저를 찾을 수 없습니다."),

    /* 500 INTERNAL_SERVER_ERROR */
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final Integer httpStatus;
    private final String message;
}
