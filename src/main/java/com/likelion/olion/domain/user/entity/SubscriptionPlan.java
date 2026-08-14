package com.likelion.olion.domain.user.entity;

public enum SubscriptionPlan {
    BASIC(1),
    PLUS(5),
    PRO(Integer.MAX_VALUE);

    private final int dailyDiagnosisLimit;

    SubscriptionPlan(int dailyDiagnosisLimit) {
        this.dailyDiagnosisLimit = dailyDiagnosisLimit;
    }

    public int dailyDiagnosisLimit() {
        return dailyDiagnosisLimit;
    }
}
