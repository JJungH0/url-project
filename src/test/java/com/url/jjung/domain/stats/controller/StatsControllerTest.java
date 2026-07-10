package com.url.jjung.domain.stats.controller;

import com.url.jjung.domain.stats.dto.StatsResp;
import com.url.jjung.domain.stats.repository.ClickLogRepository;
import com.url.jjung.domain.stats.service.StatsService;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import com.url.jjung.global.exception.CustomException;
import com.url.jjung.global.exception.ErrorCode;
import com.url.jjung.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @InjectMocks
    private StatsController statsController;

    @Mock
    private StatsService statsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(statsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@test.com", null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private StatsResp createStatsResp(long totalClicks) {
        return new StatsResp("aB3kZ1",
                "https://www.google.com",
                totalClicks,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30));
    }

    @Test
    @DisplayName("통계 조회 성공")
    void getStats_success() throws Exception {
        StatsResp statsResp = createStatsResp(5L);
        given(statsService.getStats(any(), anyString())).willReturn(statsResp);

        mockMvc.perform(get("/api/urls/aB3kZ1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value("aB3kZ1"))
                .andExpect(jsonPath("$.data.originalUrl").value("https://www.google.com"))
                .andExpect(jsonPath("$.data.totalClicks").value(5L));
    }

    @Test
    @DisplayName("통계 조회 성공 - 클릭 수 0")
    void getStats_success_zeroClicks() throws Exception {
        // given
        StatsResp statsResp = createStatsResp(0L);
        given(statsService.getStats(any(), anyString())).willReturn(statsResp);

        // when & then
        mockMvc.perform(get("/api/urls/aB3kZ1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalClicks").value(0L));
    }

    @Test
    @DisplayName("통계 조회 실패 - 존재하지 않는 단축 코드")
    void getStats_fail_notFound() throws Exception {
        // given
        given(statsService.getStats(any(), anyString())).willThrow(new CustomException(ErrorCode.URL_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/urls/aB3kZ1/stats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.URL_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("통계 조회 실패 - 본인 URL이 아님")
    void getStats_fail_forbidden() throws Exception {
        // given
        given(statsService.getStats(any(), anyString())).willThrow(new CustomException(ErrorCode.URL_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/urls/aB3kZ1/stats"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.URL_FORBIDDEN.getMessage()));
    }
}