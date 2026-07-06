package com.url.jjung.domain.url.dto;

import com.url.jjung.domain.url.entity.ShortUrl;

import java.time.LocalDateTime;

public record CreateUrlResp(
        String originalUrl,
        String shortUrl,
        String shortCode,
        LocalDateTime expiresAt
) {

    public static CreateUrlResp of(ShortUrl shortUrl, String baseUrl) {
        return new CreateUrlResp(
                shortUrl.getOriginalUrl(),
                baseUrl + "/r/" + shortUrl.getShortCode(),
                shortUrl.getShortCode(),
                shortUrl.getExpiresAt()
        );
    }
}
