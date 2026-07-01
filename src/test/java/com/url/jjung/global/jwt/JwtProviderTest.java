package com.url.jjung.global.jwt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
    void accessToken_생성() {
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
    void 이메일_추출() {
        // given
        String token = jwtProvider.generateAccessToken("test@test.com");
        // when
        String email = jwtProvider.getEmail(token);
        // then
        assertEquals("test@test.com", email);
    }

    @Test
    void 변조된토큰_검증() {
        // given
        String token = jwtProvider.generateAccessToken("test@test.com");

        // when
        token += "attack";

        // then
        assertFalse(jwtProvider.validateToken(token));

    }

    @Test
    void 만료된토큰_검증() throws InterruptedException {
        // given
        JwtProvider testJwt = new JwtProvider("abcdefghijklmnopqrstuvwxyz123456", 5, 1);
        String token = testJwt.generateAccessToken("test@test.com");

        // when
        Thread.sleep(5000);

        // then
        assertFalse(testJwt.validateToken(token));

    }
}