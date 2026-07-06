package com.url.jjung.domain.url.repository;

import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.url.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    List<ShortUrl> findByUser(User user);
}
