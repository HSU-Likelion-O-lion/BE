package com.likelion.olion.domain.emotion.repository;

import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionDiagnosisRepository extends JpaRepository<EmotionDiagnosis, Long> {
    List<EmotionDiagnosis> findByUserIdOrderByCreatedAtDesc(Long userId);
}
