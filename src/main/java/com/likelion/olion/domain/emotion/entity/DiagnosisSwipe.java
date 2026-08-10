package com.likelion.olion.domain.emotion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "diagnosis_swipe")
public class DiagnosisSwipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long swipeId;
    private Long diagnosisId;
    private Integer cardId;
    private boolean liked;

    protected DiagnosisSwipe() {
    }

    public DiagnosisSwipe(Long diagnosisId, Integer cardId, boolean liked) {
        this.diagnosisId = diagnosisId;
        this.cardId = cardId;
        this.liked = liked;
    }
}
