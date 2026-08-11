package com.likelion.olion.domain.essay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "essays")
public class Essay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long essayId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EssayStatus status;

    private Instant publishedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected Essay() {
    }

    public Essay(Long userId) {
        this.userId = userId;
        this.status = EssayStatus.QUEUED;
        this.createdAt = Instant.now();
    }

    public void startProcessing() {
        this.status = EssayStatus.PROCESSING;
    }

    public void complete() {
        this.status = EssayStatus.COMPLETED;
    }

    public void fail() {
        this.status = EssayStatus.FAILED;
    }

    public Long getEssayId() { return essayId; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public EssayStatus getStatus() { return status; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
