package com.url.jjung.domain.auth.service;

import com.url.jjung.domain.auth.dto.LoginReq;
import com.url.jjung.domain.auth.dto.LoginResp;
import com.url.jjung.domain.auth.dto.RegisterReq;
import com.url.jjung.domain.auth.entity.User;
import com.url.jjung.domain.auth.repository.UserRepository;
import com.url.jjung.global.jwt.JwtProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
        // given
        RegisterReq req = new RegisterReq("test@test.com", "password123", "홍길동");
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("encodedPassword");

        // when
        authService.register(req);

        // then
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_fail_duplicateEmail() {
        // given
        RegisterReq req = new RegisterReq("test@test.com", "password123", "홍길동");

        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginReq req = new LoginReq("test@test.com", "password123");

        User user = User.builder()
                .email(req.email())
                .pw("encodedPassword")
                .nickname("홍길동")
                .build();

        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getPassword())).thenReturn(true);
        when(jwtProvider.generateAccessToken(user.getEmail())).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken(user.getEmail())).thenReturn("refreshToken");

        // when
        LoginResp resp = authService.login(req);

        // then
        assertThat(resp.accessToken()).isEqualTo("accessToken");
        assertThat(resp.refreshToken()).isEqualTo("refreshToken");
        assertThat(resp.email()).isEqualTo("test@test.com");
        assertThat(resp.nickname()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_emailNotFound() {
        // given
        LoginReq req = new LoginReq("test@test.com", "password123");

        when(userRepository.findByEmail(req.email())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        // given
        LoginReq req = new LoginReq("test@test.com", "password123");

        User user = User.builder()
                .email(req.email())
                .pw("encodedPassword")
                .nickname("홍길동")
                .build();

        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}