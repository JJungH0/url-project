package com.url.jjung.domain.stats.dto;

import com.url.jjung.domain.url.entity.ShortUrl;

import java.time.LocalDateTime;

public record StatsResp(
        String shortCode,
        String originalUrl,
        long totalClicks,
        LocalDateTime createAt,
        LocalDateTime expiresAt
) {
    public static StatsResp of(ShortUrl shortUrl, long totalClicks) {
        return new StatsResp(
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                totalClicks,
                shortUrl.getCreateAt(),
                shortUrl.getExpiresAt()
        );
    }
}
