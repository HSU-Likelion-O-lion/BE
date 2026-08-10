package com.likelion.olion.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken을 입력해주세요.")
        String refreshToken
) {
}
