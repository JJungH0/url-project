package com.url.jjung.domain.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUrlReq(
        @NotBlank(message = "URL을 입력해주세요")
        @Pattern(
                regexp = "^(https?://).+",
                message = "URL은 http:// 또는 https://로 시작해야 합니다."
        )
        String originalUrl,
        Integer expirationDays
) {
}
