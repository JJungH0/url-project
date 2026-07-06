package com.url.jjung.global.exception;

import com.url.jjung.global.util.ApiResp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("CustomException 발생 시 DUPLICATE_EMAIL 응답")
    void handleCustomException_duplicateEmail() {
        // given
        CustomException e = new CustomException(ErrorCode.DUPLICATE_EMAIL);

        // when
        ResponseEntity<ApiResp<Void>> response = globalExceptionHandler.handleCustomException(e);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.DUPLICATE_EMAIL.getMessage());
    }

    @Test
    @DisplayName("CustomException 발생 시 INVALID_CREDENTIALS 응답")
    void handleCustomException_invalidCredentials() {
        // given
        CustomException e = new CustomException(ErrorCode.INVALID_CREDENTIALS);

        // when
        ResponseEntity<ApiResp<Void>> response = globalExceptionHandler.handleCustomException(e);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INVALID_CREDENTIALS.getMessage());
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 500으로 응답")
    void handleException_internalServerError() {
        // given
        Exception e = new Exception("예상치 못한 오류");

        // when
        ResponseEntity<ApiResp<Void>> response = globalExceptionHandler.handleException(e);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}