package com.likelion.olion.domain.emotion.repository;

import com.likelion.olion.domain.emotion.entity.DiagnosisSwipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisSwipeRepository extends JpaRepository<DiagnosisSwipe, Long> {
    List<DiagnosisSwipe> findByDiagnosisIdAndLikedTrueOrderBySwipeIdAsc(Long diagnosisId);
}
