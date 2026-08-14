package com.likelion.olion.domain.user.dto.request;

import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public record UpdatePlanRequest(
        @NotNull(message = "변경할 구독 등급을 입력해주세요.")
        SubscriptionPlan plan
) {
}
