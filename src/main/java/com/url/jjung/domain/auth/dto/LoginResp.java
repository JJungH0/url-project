package com.url.jjung.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

public record LoginResp(
        String accessToken,
        String refreshToken,
        String email,
        String nickname) {

}
