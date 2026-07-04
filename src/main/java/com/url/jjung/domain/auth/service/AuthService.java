package com.url.jjung.domain.auth.service;

import com.url.jjung.domain.auth.dto.LoginReq;
import com.url.jjung.domain.auth.dto.LoginResp;
import com.url.jjung.domain.auth.dto.RegisterReq;
import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.auth.repository.UserRepository;
import com.url.jjung.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void register(RegisterReq req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        User user = User.builder()
                .email(req.email())
                .pw(passwordEncoder.encode(req.password()))
                .nickname(req.nickname())
                .build();

        userRepository.save(user);
    }

    public LoginResp login(LoginReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        return new LoginResp(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getNickname()
        );
    }
}
