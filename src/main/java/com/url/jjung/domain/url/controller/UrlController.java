package com.url.jjung.domain.url.controller;

import com.url.jjung.domain.url.dto.CreateUrlReq;
import com.url.jjung.domain.url.dto.CreateUrlResp;
import com.url.jjung.domain.url.service.UrlService;
import com.url.jjung.global.util.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<ApiResp<CreateUrlResp>> createShortUrl(
            @AuthenticationPrincipal String email,
            @RequestBody @Valid CreateUrlReq req
    ) {
        CreateUrlResp resp = urlService.createShortUrl(email, req);

        return ResponseEntity.ok(ApiResp.success(resp, "단축 URL 생성에 성공하셨습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResp<List<CreateUrlResp>>> getMyUrls(@AuthenticationPrincipal String email) {
        List<CreateUrlResp> resp = urlService.getMyUrls(email);

        return ResponseEntity.ok(ApiResp.success(resp, "단축 URL 조회에 성공하셨습니다"));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(
            @AuthenticationPrincipal String email,
            @PathVariable String shortCode
    ) {
        urlService.deleteShortUrl(email, shortCode);

        return ResponseEntity.noContent().build();
    }
}
