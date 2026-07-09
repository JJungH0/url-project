package com.url.jjung.domain.stats.service;

import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.stats.dto.StatsResp;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @InjectMocks
    private StatsService statsService;

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

    private ShortUrl createShortUrl(User user) {
        return ShortUrl.builder()
                .user(user)
                .originalUrl("https://www.google.com")
                .shortCode("aB3kZ1")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("통계 조회 성공")
    void getStats_success() {
        // given
        User user = createUser();
        ShortUrl shortUrl = createShortUrl(user);

        given(shortUrlRepository.findByShortCode("aB3kZ1")).willReturn(Optional.of(shortUrl));
        given(clickLogRepository.countByShortUrl(shortUrl)).willReturn(5L);

        // when
        StatsResp resp = statsService.getStats("test@test.com", "aB3kZ1");

        // then
        assertThat(resp.shortCode()).isEqualTo("aB3kZ1");
        assertThat(resp.originalUrl()).isEqualTo("https://www.google.com");
        assertThat(resp.totalClicks()).isEqualTo(5L);
    }

    @Test
    @DisplayName("통계 조회 실패 - 본인 URL이 아님")
    void getStats_fail_forbidden() {
        // given
        User user = createUser();
        ShortUrl shortUrl = createShortUrl(user);

        given(shortUrlRepository.findByShortCode("aB3kZ1")).willReturn(Optional.of(shortUrl));

        // when & then
        assertThatThrownBy(() -> statsService.getStats("other@test.com", "aB3kZ1"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.URL_FORBIDDEN);
    }

    @Test
    @DisplayName("통계 조회 성공 - 클릭 수 0")
    void getStats_success_zeroClicks() {
        // given
        User user = createUser();
        ShortUrl shortUrl = createShortUrl(user);

        given(shortUrlRepository.findByShortCode("aB3kZ1")).willReturn(Optional.of(shortUrl));
        given(clickLogRepository.countByShortUrl(shortUrl)).willReturn(0L);

        // when
        StatsResp resp = statsService.getStats("test@test.com", "aB3kZ1");

        // then
        Assertions.assertThat(resp.totalClicks()).isEqualTo(0L);
    }
}