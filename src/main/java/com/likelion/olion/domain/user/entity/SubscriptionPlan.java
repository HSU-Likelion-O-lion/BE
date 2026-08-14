package com.likelion.olion.domain.user.entity;

public enum SubscriptionPlan {
    BASIC(1, 3),
    PLUS(5, 5),
    PRO(Integer.MAX_VALUE, 7);

    private final int dailyDiagnosisLimit;
    private final int maxMatePinCount;

    SubscriptionPlan(int dailyDiagnosisLimit, int maxMatePinCount) {
        this.dailyDiagnosisLimit = dailyDiagnosisLimit;
        this.maxMatePinCount = maxMatePinCount;
    }

    public int dailyDiagnosisLimit() {
        return dailyDiagnosisLimit;
    }

    public int maxMatePinCount() {
        return maxMatePinCount;
    }
}
