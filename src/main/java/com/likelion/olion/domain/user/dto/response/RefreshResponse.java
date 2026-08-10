package com.likelion.olion.domain.user.dto.response;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {
}
