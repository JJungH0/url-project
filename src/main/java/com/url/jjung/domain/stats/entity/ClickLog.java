package com.url.jjung.domain.stats.entity;

import com.url.jjung.domain.url.entity.ShortUrl;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ClickLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id",nullable = false)
    private ShortUrl shortUrl;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @PrePersist
    private void onCreate() {
        this.clickedAt = LocalDateTime.now();
    }

    @Builder
    public ClickLog(ShortUrl shortUrl) {
        this.shortUrl = shortUrl;
    }
}
