package com.likelion.olion.domain.emotion.controller;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisHistoryResponse;
import com.likelion.olion.domain.emotion.service.EmotionDiagnosisHistoryService;
import com.likelion.olion.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/emotion-diagnoses")
@Tag(name = "감정 진단 이력", description = "사용자의 감정 진단 이력 조회 API")
public class EmotionDiagnosisHistoryController {
    private final EmotionDiagnosisHistoryService historyService;

    public EmotionDiagnosisHistoryController(EmotionDiagnosisHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    @Operation(summary = "감정 진단 이력 조회", description = "로그인한 사용자가 과거에 제출한 감정 진단 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<EmotionDiagnosisHistoryResponse>> getHistory(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(
                "진단 이력을 조회했습니다.",
                historyService.getHistory(userId)
        ));
    }
}
