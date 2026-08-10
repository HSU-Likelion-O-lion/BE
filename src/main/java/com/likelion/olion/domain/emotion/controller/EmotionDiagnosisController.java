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

@RestController
@RequestMapping("/api/emotion-diagnoses")
public class EmotionDiagnosisController {
    private final EmotionDiagnosisService emotionDiagnosisService;

    public EmotionDiagnosisController(EmotionDiagnosisService emotionDiagnosisService) {
        this.emotionDiagnosisService = emotionDiagnosisService;
    }

    @PostMapping
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
