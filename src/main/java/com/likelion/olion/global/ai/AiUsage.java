package com.likelion.olion.global.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ai_usages", indexes = {
        @Index(name = "idx_ai_usage_user_requested_at", columnList = "user_id, requested_at"),
        @Index(name = "idx_ai_usage_feature_requested_at", columnList = "feature, requested_at")
})
public class AiUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String feature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiUsageStatus status;

    @Column(nullable = false)
    private Instant requestedAt;

    private Instant completedAt;
    private Long durationMillis;

    protected AiUsage() {
    }

    public AiUsage(Long userId, String feature, AiUsageStatus status, Instant requestedAt) {
        this.userId = userId;
        this.feature = feature;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public void complete(AiUsageStatus status, Instant completedAt, long durationMillis) {
        this.status = status;
        this.completedAt = completedAt;
        this.durationMillis = Math.max(0, durationMillis);
    }

    public Long getUsageId() { return usageId; }
    public Long getUserId() { return userId; }
    public String getFeature() { return feature; }
    public AiUsageStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Long getDurationMillis() { return durationMillis; }
}
