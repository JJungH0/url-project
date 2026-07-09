package com.url.jjung.domain.stats.service;

import com.url.jjung.domain.stats.dto.StatsResp;
import com.url.jjung.domain.stats.repository.ClickLogRepository;
import com.url.jjung.domain.url.entity.ShortUrl;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import com.url.jjung.global.exception.CustomException;
import com.url.jjung.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final ShortUrlRepository shortUrlRepository;
    private final ClickLogRepository clickLogRepository;


    public StatsResp getStats(String email, String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new CustomException(ErrorCode.URL_NOT_FOUND));

        if (!shortUrl.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.URL_FORBIDDEN);
        }

        long totalClicks = clickLogRepository.countByShortUrl(shortUrl);

        return StatsResp.of(shortUrl, totalClicks);
    }
}
