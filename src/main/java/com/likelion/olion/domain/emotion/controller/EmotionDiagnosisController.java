package com.likelion.olion.domain.emotion.controller;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisRequest;
import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisResponse;
import com.likelion.olion.domain.emotion.service.EmotionDiagnosisService;
import com.likelion.olion.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/emotion-diagnoses")
@Tag(name = "감정 진단", description = "감정 진단 제출과 추천 도서 API")
public class EmotionDiagnosisController {
    private final EmotionDiagnosisService emotionDiagnosisService;

    public EmotionDiagnosisController(EmotionDiagnosisService emotionDiagnosisService) {
        this.emotionDiagnosisService = emotionDiagnosisService;
    }

    @PostMapping
    @Operation(summary = "감정 진단 제출", description = "감정 카드 선택 결과를 제출하고 진단 ID와 추천 도서를 반환합니다.")
    public ResponseEntity<ApiResponse<EmotionDiagnosisResponse>> submit(
            Principal principal,
            @Valid @RequestBody EmotionDiagnosisRequest request
    ) {
        Long userId = Long.valueOf(principal.getName());
        EmotionDiagnosisService.Submission submission = emotionDiagnosisService.submit(userId, request);
        return ResponseEntity.status(submission.status()).body(ApiResponse.success(
                submission.code(), submission.status(), submission.message(), submission.data()
        ));
    }
}
