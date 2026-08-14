package com.likelion.olion.domain.user.dto.response;

import com.likelion.olion.domain.user.entity.SubscriptionPlan;

import java.time.LocalDateTime;

public record UserMeResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        SubscriptionPlan plan,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
