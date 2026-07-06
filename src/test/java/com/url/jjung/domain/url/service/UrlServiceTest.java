package com.url.jjung.domain.url.service;

import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.auth.repository.UserRepository;
import com.url.jjung.domain.url.dto.CreateUrlReq;
import com.url.jjung.domain.url.dto.CreateUrlResp;
import com.url.jjung.domain.url.entity.ShortUrl;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @InjectMocks
    private UrlService urlService;

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
    }

    private User createUser() {
        return User.builder()
                .email("test@test.com")
                .pw("password123!")
                .nickname("테스터")
                .build();
    }

    private ShortUrl createShortUrl(User user) {
        return ShortUrl.builder()
                .user(user)
                .originalUrl("https://www.example.com/very/long/url")
                .shortCode("test1")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("단축 URL 생성 성공 - 만료 기간 직접 설정")
    void createUrl_success_withExpirationDays() {
        User user = createUser();
        CreateUrlReq req = new CreateUrlReq(
                "https://www.example.com/very/long/url",
                7
        );

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(shortUrlRepository.existsByShortCode(anyString())).willReturn(false);
        given(shortUrlRepository.save(any(ShortUrl.class))).willAnswer(i -> i.getArgument(0));


        CreateUrlResp resp = urlService.createShortUrl(user.getEmail(), req);

        assertThat(resp.originalUrl()).isEqualTo(req.originalUrl());
        assertThat(resp.shortUrl()).startsWith("http://localhost:8080/r/");
        assertThat(resp.expiresAt()).isAfter(LocalDateTime.now().plusDays(6));
    }
}