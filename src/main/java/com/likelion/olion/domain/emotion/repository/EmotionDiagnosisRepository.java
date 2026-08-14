package com.likelion.olion.domain.emotion.repository;

import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EmotionDiagnosisRepository extends JpaRepository<EmotionDiagnosis, Long> {
    List<EmotionDiagnosis> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end);
}
