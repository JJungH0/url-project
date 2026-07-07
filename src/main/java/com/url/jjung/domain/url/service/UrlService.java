package com.url.jjung.domain.url.service;

import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.auth.repository.UserRepository;
import com.url.jjung.domain.url.dto.CreateUrlReq;
import com.url.jjung.domain.url.dto.CreateUrlResp;
import com.url.jjung.domain.url.entity.ShortUrl;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import com.url.jjung.global.exception.CustomException;
import com.url.jjung.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY = 5;

    @Transactional
    public CreateUrlResp createShortUrl(String email, CreateUrlReq req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String shortCode = generateUniqueShortCode();

        int days = Objects.nonNull(req.expirationDays()) ? req.expirationDays() : 30;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(days);

        ShortUrl shortUrl = ShortUrl.builder()
                .user(user)
                .originalUrl(req.originalUrl())
                .shortCode(shortCode)
                .expiresAt(expiresAt)
                .build();

        shortUrlRepository.save(shortUrl);

        return CreateUrlResp.of(shortUrl, baseUrl);
    }

    public List<CreateUrlResp> getMyUrls(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return shortUrlRepository.findByUser(user)
                .stream()
                .map(shortUrl -> CreateUrlResp.of(shortUrl, baseUrl))
                .toList();
    }

    @Transactional
    public void deleteShortUrl(String email, String shortCode){
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new CustomException(ErrorCode.URL_NOT_FOUND));

        if (!shortUrl.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.URL_FORBIDDEN);
        }

        shortUrlRepository.delete(shortUrl);

    }

    private String generateUniqueShortCode() {
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < MAX_RETRY; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < CODE_LENGTH; j++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            if (!shortUrlRepository.existsByShortCode(sb.toString())) {
                return sb.toString();
            }
        }
        throw new CustomException(ErrorCode.SHORT_CODE_GENERATION_FAILED);
    }
}
