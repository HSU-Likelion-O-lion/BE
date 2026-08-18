package com.likelion.olion.domain.user.entity;

public enum SubscriptionPlan {
    BASIC(1, 3, 1),
    PLUS(5, 5, 5),
    PRO(Integer.MAX_VALUE, 7, Integer.MAX_VALUE);

    private final int dailyDiagnosisLimit;
    private final int maxMatePinCount;
    private final int dailyEssayGenerationLimit;

    SubscriptionPlan(int dailyDiagnosisLimit, int maxMatePinCount, int dailyEssayGenerationLimit) {
        this.dailyDiagnosisLimit = dailyDiagnosisLimit;
        this.maxMatePinCount = maxMatePinCount;
        this.dailyEssayGenerationLimit = dailyEssayGenerationLimit;
    }

    public int dailyDiagnosisLimit() {
        return dailyDiagnosisLimit;
    }

    public int maxMatePinCount() {
        return maxMatePinCount;
    }

    public int dailyEssayGenerationLimit() {
        return dailyEssayGenerationLimit;
    }
}
