package com.likelion.olion.domain.user.dto.response;

import java.time.LocalDateTime;

public record UpdateUserResponse(
        Long userId,
        String nickname,
        LocalDateTime updatedAt
) {
}
