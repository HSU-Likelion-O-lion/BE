package com.likelion.olion.domain.emotion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "emotion_diagnosis")
public class EmotionDiagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diagnosisId;
    private Long userId;
    private Instant createdAt;

    protected EmotionDiagnosis() {
    }

    public EmotionDiagnosis(Long userId) {
        this.userId = userId;
        this.createdAt = Instant.now();
    }

    public Long getDiagnosisId() { return diagnosisId; }
    public Long getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
}
