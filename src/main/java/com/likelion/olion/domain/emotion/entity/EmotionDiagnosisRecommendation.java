package com.likelion.olion.domain.emotion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "emotion_diagnosis_recommendations", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_emotion_recommendation_diagnosis_order",
                columnNames = {"diagnosis_id", "recommendation_order"})
})
public class EmotionDiagnosisRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EmotionDiagnosis diagnosis;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String coverImageUrl;

    @Column(columnDefinition = "TEXT")
    private String shortDesc;

    @Column(name = "recommendation_order", nullable = false)
    private Integer recommendationOrder;

    protected EmotionDiagnosisRecommendation() {
    }

    public EmotionDiagnosisRecommendation(
            EmotionDiagnosis diagnosis,
            Long bookId,
            String title,
            String coverImageUrl,
            String shortDesc,
            Integer recommendationOrder
    ) {
        this.diagnosis = diagnosis;
        this.bookId = bookId;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.shortDesc = shortDesc;
        this.recommendationOrder = recommendationOrder;
    }

    public Long getRecommendationId() { return recommendationId; }
    public EmotionDiagnosis getDiagnosis() { return diagnosis; }
    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getShortDesc() { return shortDesc; }
    public Integer getRecommendationOrder() { return recommendationOrder; }
}
