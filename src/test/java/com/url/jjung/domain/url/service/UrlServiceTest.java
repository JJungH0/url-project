package com.url.jjung.domain.url.service;

import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.auth.repository.UserRepository;
import com.url.jjung.domain.url.dto.CreateUrlReq;
import com.url.jjung.domain.url.dto.CreateUrlResp;
import com.url.jjung.domain.url.entity.ShortUrl;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import com.url.jjung.global.exception.CustomException;
import com.url.jjung.global.exception.ErrorCode;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

    @Test
    @DisplayName("단축 URL 생성 성공 - 만료 기간 미설정 시 기본 30일")
    void createShortUrl_success_defaultExpirationDays() {

        // given
        User user = createUser();

        CreateUrlReq req =
                new CreateUrlReq("https://www.example.com/very/long/url", null);

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(shortUrlRepository.existsByShortCode(anyString())).willReturn(false);
        given(shortUrlRepository.save(any(ShortUrl.class))).willAnswer(i -> i.getArgument(0));

        // when
        CreateUrlResp resp = urlService.createShortUrl(user.getEmail(), req);

        // then
        assertThat(resp.expiresAt()).isAfter(LocalDateTime.now().plusDays(29));
    }

    @Test
    @DisplayName("단축 URL 생성 실패 - 존재하지 않는 사용자")
    void createShortUrl_fail_userNotFound() {
        // given
        CreateUrlReq req = new CreateUrlReq("https://www.example.com/very/long/url", 30);

        // when
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.createShortUrl("test@test.com", req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("단축 URL 생성 실패 - 코드 충돌 5회 초과")
    void createShortUrl_fail_shortCodeGenerationFailed() {

        // given
        User user = createUser();
        CreateUrlReq req = new CreateUrlReq("https://www.example.com/very/long/url", 30);

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(shortUrlRepository.existsByShortCode(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> urlService.createShortUrl(user.getEmail(), req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SHORT_CODE_GENERATION_FAILED);
    }

    @Test
    @DisplayName("내 단축 URL 목록 조회 성공")
    void getMyUrls_success() {

        // given
        User user = createUser();
        ShortUrl shortUrl = createShortUrl(user);

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(shortUrlRepository.findByUser(user)).willReturn(List.of(shortUrl));

        // when
        List<CreateUrlResp> resp = urlService.getMyUrls(user.getEmail());

        // then
        assertThat(resp).hasSize(1);
        assertThat(resp.getFirst().originalUrl()).isEqualTo("https://www.example.com/very/long/url");
        assertThat(resp.getFirst().shortUrl()).isEqualTo("http://localhost:8080/r/test1");
    }

    @Test
    @DisplayName("내 단축 URL 목록 조회 실패 - 존재하지 않는 사용자")
    void getMyUrls_fail_userNotFound() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getMyUrls("test@test.com"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("단축 URL 삭제 성공")
    void deleteShortUrl_success() {
        User user = createUser();
        ShortUrl shortUrl = createShortUrl(user);

        given(shortUrlRepository.findByShortCode(anyString())).willReturn(Optional.of(shortUrl));

        // when
        urlService.deleteShortUrl(user.getEmail(), shortUrl.getShortCode());

        // then
        then(shortUrlRepository).should().delete(shortUrl);
    }

    @Test
    @DisplayName("단축 URL 삭제 실패 - 존재하지 않는 단축 URL")
    void deleteShortUrl_fail_urlNotFound() {
        // given
        given(shortUrlRepository.findByShortCode(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> urlService.deleteShortUrl("test@test.com", "test1"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.URL_NOT_FOUND);
    }

    @Test
    @DisplayName("단축 URL 삭제 실패 - 본인 URL이 아님")
    void deleteUrl_fail_forbidden() {
        User user = createUser();
        ShortUrl shortUrl = createShortUrl(user);

        given(shortUrlRepository.findByShortCode(shortUrl.getShortCode()))
                .willReturn(Optional.of(shortUrl));

        assertThatThrownBy(() -> urlService.deleteShortUrl("other@test.com", shortUrl.getShortCode()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.URL_FORBIDDEN);
    }
}