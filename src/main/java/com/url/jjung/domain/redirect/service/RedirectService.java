package com.url.jjung.domain.redirect.service;

import com.url.jjung.domain.stats.entity.ClickLog;
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
@Transactional
public class RedirectService {

    private final ShortUrlRepository shortUrlRepository;
    private final ClickLogRepository clickLogRepository;

    public String getOriginalUrl(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new CustomException(ErrorCode.URL_NOT_FOUND));

        if (shortUrl.isExpired()) {
            throw new CustomException(ErrorCode.URL_EXPIRED);
        }

        ClickLog clickLog = ClickLog.builder()
                .shortUrl(shortUrl)
                .build();

        clickLogRepository.save(clickLog);

        return shortUrl.getOriginalUrl();
    }
}
