package com.url.jjung.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생하였습니다."),
    SHORT_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "단축 코드 생성에 실패하였습니다."),

    URL_DUPLICATE(HttpStatus.CONFLICT, "이미 단축된 URL입니다."),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 단축 URL입니다."),
    URL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 URL만 삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
