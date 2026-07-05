package com.url.jjung.domain.auth.controller;

import com.url.jjung.domain.auth.dto.LoginReq;
import com.url.jjung.domain.auth.dto.LoginResp;
import com.url.jjung.domain.auth.dto.RegisterReq;
import com.url.jjung.domain.auth.service.AuthService;
import com.url.jjung.global.util.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResp<Void>> register(@RequestBody @Valid RegisterReq req) {
        authService.register(req);
        return ResponseEntity.ok(ApiResp.success(null, "회원가입에 성공하셨습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResp<LoginResp>> login(@RequestBody @Valid LoginReq req) {
        LoginResp resp = authService.login(req);
        return ResponseEntity.ok(ApiResp.success(resp, "로그인에 성공하셨습니다."));
    }
}
