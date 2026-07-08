package com.url.jjung.domain.redirect.service;

import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.stats.entity.ClickLog;
import com.url.jjung.domain.stats.repository.ClickLogRepository;
import com.url.jjung.domain.url.entity.ShortUrl;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import com.url.jjung.global.exception.CustomException;
import com.url.jjung.global.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RedirectServiceTest {

    @InjectMocks
    private RedirectService redirectService;

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private ClickLogRepository clickLogRepository;

    private User createUser() {
        return User.builder()
                .email("test@test.com")
                .pw("password123!")
                .nickname("테스터")
                .build();
    }

    private ShortUrl createShortUrl(LocalDateTime expiresAt) {
        return ShortUrl.builder()
                .user(createUser())
                .originalUrl("https://www.google.com")
                .shortCode("aB3kZ1")
                .expiresAt(expiresAt)
                .build();
    }

    @Test
    @DisplayName("리다이렉트 성공 - 원본 URL 반환 및 클릭 로그 저장")
    void getOriginal_success() {
        // given
        ShortUrl shortUrl = createShortUrl(LocalDateTime.now().plusDays(30));

        given(shortUrlRepository.findByShortCode("aB3kZ1"))
                .willReturn(Optional.of(shortUrl));

        // when
        String originalUrl = redirectService.getOriginalUrl("aB3kZ1");

        assertThat(originalUrl).isEqualTo("https://www.google.com");
        then(clickLogRepository).should().save(any(ClickLog.class));
    }

    @Test
    @DisplayName("리다이렉트 실패 - 존재하지 않는 코드")
    void getOriginalUrl_fail_notFound() {

        given(shortUrlRepository.findByShortCode("aB3kZ1")).willReturn(Optional.empty());

        assertThatThrownBy(() -> redirectService.getOriginalUrl("aB3kZ1"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.URL_NOT_FOUND);

        then(clickLogRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("리다이렉트 실패 - 만료된 단축 URL")
    void getOriginal_fail_expired() {
        ShortUrl shortUrl = createShortUrl(LocalDateTime.now().minusDays(1));

        given(shortUrlRepository.findByShortCode("aB3kZ1"))
                .willReturn(Optional.of(shortUrl));

        assertThatThrownBy(() -> redirectService.getOriginalUrl("aB3kZ1"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.URL_EXPIRED);

        then(clickLogRepository).should(never()).save(any());
    }
}