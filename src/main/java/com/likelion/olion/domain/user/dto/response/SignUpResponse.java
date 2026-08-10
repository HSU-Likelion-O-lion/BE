package com.likelion.olion.domain.user.dto.response;

import java.time.LocalDateTime;

public record SignUpResponse(
        Long userId,
        String email,
        String nickname,
        LocalDateTime createdAt
) {
}
