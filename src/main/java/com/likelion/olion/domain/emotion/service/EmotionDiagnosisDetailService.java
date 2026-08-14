package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisDetailResponse;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosisRecommendation;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRecommendationRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmotionDiagnosisDetailService {
    private final EmotionDiagnosisRepository diagnosisRepository;
    private final EmotionDiagnosisRecommendationRepository recommendationRepository;

    public EmotionDiagnosisDetailService(
            EmotionDiagnosisRepository diagnosisRepository,
            EmotionDiagnosisRecommendationRepository recommendationRepository
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public EmotionDiagnosisDetailResponse getDiagnosis(Long userId, Long diagnosisId) {
        EmotionDiagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "진단 결과를 찾을 수 없습니다."));
        if (!diagnosis.getUserId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN, "본인의 진단 결과만 조회할 수 있습니다.");
        }

        return new EmotionDiagnosisDetailResponse(
                diagnosis.getDiagnosisId(),
                diagnosis.getCreatedAt(),
                recommendationRepository
                        .findByDiagnosisDiagnosisIdOrderByRecommendationOrderAsc(diagnosisId)
                        .stream()
                        .map(this::toRecommendedBook)
                        .toList());
    }

    private EmotionDiagnosisDetailResponse.RecommendedBook toRecommendedBook(
            EmotionDiagnosisRecommendation recommendation
    ) {
        return new EmotionDiagnosisDetailResponse.RecommendedBook(
                recommendation.getBookId(),
                recommendation.getTitle(),
                recommendation.getCoverImageUrl(),
                recommendation.getShortDesc());
    }
}
