package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisHistoryResponse;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmotionDiagnosisHistoryService {
    private final EmotionDiagnosisRepository diagnosisRepository;

    public EmotionDiagnosisHistoryService(EmotionDiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;
    }

    @Transactional(readOnly = true)
    public EmotionDiagnosisHistoryResponse getHistory(Long userId) {
        return new EmotionDiagnosisHistoryResponse(
                diagnosisRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private EmotionDiagnosisHistoryResponse.Diagnosis toResponse(EmotionDiagnosis diagnosis) {
        return new EmotionDiagnosisHistoryResponse.Diagnosis(
                diagnosis.getDiagnosisId(), diagnosis.getCreatedAt()
        );
    }
}
