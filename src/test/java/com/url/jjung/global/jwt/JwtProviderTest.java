package com.url.jjung.global.jwt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JwtProviderTest {
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                "abcdefghijklmnopqrstuvwxyz123456",
                1000 * 60 * 30,
                1000 * 60 * 60 * 24
        );
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken_success() {
        // given & when
        String token = jwtProvider.generateAccessToken("test@test.com");

        // then
        assertAll(
                () -> assertNotNull(token),
                () -> assertFalse(token.isBlank()),
                () -> assertTrue(token.contains("."))
        );

    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void getEmail_success() {
        // given
        String token = jwtProvider.generateAccessToken("test@test.com");
        // when
        String email = jwtProvider.getEmail(token);
        // then
        assertEquals("test@test.com", email);
    }

    @Test
    @DisplayName("변조된 토큰 검증 시 false 반환")
    void validateToken_tamperedToken_returnsFalse() {
        // given
        String token = jwtProvider.generateAccessToken("test@test.com");

        // when
        token += "attack";

        // then
        assertFalse(jwtProvider.validateToken(token));

    }

    @Test
    @DisplayName("만료된 토큰 검증 시 false 반환")
    void validateToken_expiredToken_returnsFalse() throws InterruptedException {
        // given
        JwtProvider testJwt = new JwtProvider("abcdefghijklmnopqrstuvwxyz123456", 5, 1);
        String token = testJwt.generateAccessToken("test@test.com");

        // when
        Thread.sleep(5000);

        // then
        assertFalse(testJwt.validateToken(token));
    }

    @Test
    @DisplayName("Refresh Token 생성 후 이메일 추출 성공")
    void generateRefreshToken_thenGetEmail() {
        String email = "test@test.com";

        String token = jwtProvider.generateRefreshToken(email);
        String extractedEmail = jwtProvider.getEmail(token);

        assertEquals(extractedEmail, email);
    }
}