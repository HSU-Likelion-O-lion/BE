package com.likelion.olion.domain.emotion.controller;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisHistoryResponse;
import com.likelion.olion.domain.emotion.service.EmotionDiagnosisHistoryService;
import com.likelion.olion.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/emotion-diagnoses")
public class EmotionDiagnosisHistoryController {
    private final EmotionDiagnosisHistoryService historyService;

    public EmotionDiagnosisHistoryController(EmotionDiagnosisHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmotionDiagnosisHistoryResponse>> getHistory(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(
                "진단 이력을 조회했습니다.",
                historyService.getHistory(userId)
        ));
    }
}
