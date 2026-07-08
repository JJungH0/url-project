package com.url.jjung.domain.stats.repository;

import com.url.jjung.domain.stats.entity.ClickLog;
import com.url.jjung.domain.url.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {
    List<ClickLog> findByShortUrl(ShortUrl shortUrl);

    long countByShortUrl(ShortUrl shortUrl);
}
