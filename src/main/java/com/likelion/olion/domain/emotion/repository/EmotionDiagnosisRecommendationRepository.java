package com.likelion.olion.domain.emotion.repository;

import com.likelion.olion.domain.emotion.entity.EmotionDiagnosisRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionDiagnosisRecommendationRepository
        extends JpaRepository<EmotionDiagnosisRecommendation, Long> {
    List<EmotionDiagnosisRecommendation> findByDiagnosisDiagnosisIdOrderByRecommendationOrderAsc(
            Long diagnosisId);
}
