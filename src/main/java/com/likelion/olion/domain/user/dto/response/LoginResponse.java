package com.likelion.olion.domain.user.dto.response;

public record LoginResponse(
        Long userId,
        String nickname,
        String accessToken,
        String refreshToken
) {
}
