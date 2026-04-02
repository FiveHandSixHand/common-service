package com.fhsh.daitda.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode{
    // 인증/인가(Global목적 - 추후 불필요하면 삭제)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),

    // 입력 오류
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다"),
    INVALID_PATH(HttpStatus.NOT_FOUND, "잘못된 경로입니다"),

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다"),

    // 충돌
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다");

    private final HttpStatus status;
    private final String description;
}
