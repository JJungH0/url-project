package com.url.jjung.global.filter;

import com.url.jjung.global.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!isRateLimitedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = generateKey(request);

        String countStr = redisTemplate.opsForValue().get(key);
        int count = Objects.nonNull(countStr) ? Integer.parseInt(countStr) : 0;

        if (count >= MAX_REQUESTS) {
            sendTooManyRequestsResponse(response);
            return;
        }

        if (count == 0) {
            redisTemplate.opsForValue().set(key, "1", WINDOW_SECONDS, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().increment(key);
        }

        filterChain.doFilter(request, response);
    }

    private String generateKey(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String method = req.getMethod();

        if (uri.equals("/api/auth/login") && method.equals("POST")) {
            return "rate_limit:login:" + getClientIp(req);
        }

        if (uri.equals("/api/urls") && method.equals("POST")) {
            String email = extractEmailFromToken(req);
            return "rate_limit:url:" + email;
        }

        if (uri.startsWith("/r/") && method.equals("GET")) {
            String token = extractToken(req);
            if (Objects.nonNull(token) && jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);
                return "rate_limit:redirect:" + email;
            }
            return "rate_limit:redirect:" + getClientIp(req);
        }

        return "rate:limit:unknown:" + getClientIp(req);
    }

    private boolean isRateLimitedPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String method = req.getMethod();

        if(uri.equals("/api/auth/login") && method.equals("POST")) return true;
        if(uri.equals("/api/urls") && method.equals("POST")) return true;
        if(uri.startsWith("/r/") && method.equals("GET")) return true;

        return false;
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (Objects.nonNull(ip) && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String extractToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if (Objects.nonNull(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private String extractEmailFromToken(HttpServletRequest req) {
        String token = extractToken(req);
        if (Objects.nonNull(token) && jwtProvider.validateToken(token)) {
            return jwtProvider.getEmail(token);
        }
        return getClientIp(req);
    }

    private void sendTooManyRequestsResponse(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"success\": false, \"data\": null, \"message\": \"요청이 너무 많습니다. 잠시 후 다시 시도해주세요\"}");
    }
}
