package com.url.jjung.domain.stats.controller;

import com.url.jjung.domain.stats.dto.StatsResp;
import com.url.jjung.domain.stats.service.StatsService;
import com.url.jjung.global.util.ApiResp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<ApiResp<StatsResp>> getStats(
            @AuthenticationPrincipal String email,
            @PathVariable String shortCode) {
        StatsResp resp = statsService.getStats(email, shortCode);

        return ResponseEntity.ok(ApiResp.success(resp, "통계조회가 성공적으로 완료되었습니다."));
    }
}
