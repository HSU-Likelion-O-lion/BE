package com.likelion.olion.domain.reflectionshare.entity;

import com.likelion.olion.domain.reflection.entity.Reflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reflection_shares")
public class ReflectionShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reflection_id", nullable = false)
    private Reflection reflection;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "theme_id", nullable = false)
    private Long themeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReflectionShareStatus status;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ReflectionShare() {
    }

    public ReflectionShare(Reflection reflection, Long userId, Long themeId) {
        this.reflection = reflection;
        this.userId = userId;
        this.themeId = themeId;
        this.status = ReflectionShareStatus.QUEUED;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void startProcessing() {
        this.status = ReflectionShareStatus.PROCESSING;
        this.failureReason = null;
        this.updatedAt = Instant.now();
    }

    public void complete(String imageKey) {
        this.status = ReflectionShareStatus.COMPLETED;
        this.imageKey = imageKey;
        this.failureReason = null;
        this.updatedAt = Instant.now();
    }

    public void fail(String reason) {
        this.status = ReflectionShareStatus.FAILED;
        this.failureReason = reason == null ? null : reason.substring(0, Math.min(reason.length(), 500));
        this.updatedAt = Instant.now();
    }

    public Long getShareId() { return shareId; }
    public Reflection getReflection() { return reflection; }
    public Long getUserId() { return userId; }
    public Long getThemeId() { return themeId; }
    public ReflectionShareStatus getStatus() { return status; }
    public String getImageKey() { return imageKey; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
