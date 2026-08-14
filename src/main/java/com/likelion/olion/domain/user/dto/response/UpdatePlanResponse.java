package com.likelion.olion.domain.user.dto.response;

import com.likelion.olion.domain.user.entity.SubscriptionPlan;

public record UpdatePlanResponse(
        Long userId,
        SubscriptionPlan plan
) {
}
