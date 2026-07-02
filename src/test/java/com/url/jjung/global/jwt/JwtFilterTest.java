package com.url.jjung.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    private JwtProvider jwtProvider;
    private JwtFilter jwtFilter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                "abcdefghijklmnopqrstuvwxyz123456",
                1000 * 60 * 30,
                1000 * 60 * 60 * 24
        );

        jwtFilter = new JwtFilter(jwtProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 인증 정보가 저장된다")
    void validToken_setsAuthentication() throws Exception {
        // given
        String email = "test@test.com";
        String token = jwtProvider.generateAccessToken(email);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // when
        jwtFilter.doFilterInternal(req, resp, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(authentication).isNotNull();
        Assertions.assertThat(authentication.getPrincipal()).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면 SecurityContext에 인증 정보가 저장되지 않는다.")
    void noToken_doesNotSetAuthentication() throws ServletException, IOException {
        // given
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // when
        jwtFilter.doFilterInternal(req, resp, filterChain);

        // than
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(authentication).isNull();

    }

    @Test
    @DisplayName("변조된 토큰이면 SecurityContext에 인증 정보가 저장되지 않는다")
    void tamperedToken_doesNotSetAuthentication() throws ServletException, IOException {
        // given
        String token = jwtProvider.generateAccessToken("test@test.com");
        String tamperedToken = token + "attack";

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + tamperedToken); // 변조 토큰 주입
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // when
        jwtFilter.doFilterInternal(req, resp, filterChain);

        // than
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(authentication).isNull();
    }

    @Test
    @DisplayName("Bearer 형식이 아니면 SecurityContext에 인증 정보가 저장되지 않는다.")
    void invalidFormat_doesNotSetAuthentication() throws ServletException, IOException {
        // given
        String token = jwtProvider.generateAccessToken("test@test.com");
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", token);
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // when
        jwtFilter.doFilterInternal(req, resp, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(authentication).isNull();
    }
}