package com.likelion.olion.domain.emotion.controller;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisDetailResponse;
import com.likelion.olion.domain.emotion.service.EmotionDiagnosisDetailService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/emotion-diagnoses")
@Tag(name = "감정 진단", description = "감정 진단 결과와 추천 도서 API")
public class EmotionDiagnosisDetailController {
    private final EmotionDiagnosisDetailService diagnosisDetailService;

    public EmotionDiagnosisDetailController(EmotionDiagnosisDetailService diagnosisDetailService) {
        this.diagnosisDetailService = diagnosisDetailService;
    }

    @GetMapping("/{diagnosisId}")
    @Operation(
            summary = "특정 감정 진단 결과 조회",
            description = "본인의 특정 감정 진단 시각과 당시 추천된 도서 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "진단 결과 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "다른 사용자의 진단 결과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "진단 결과를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<EmotionDiagnosisDetailResponse>> getDiagnosis(
            Principal principal,
            @Parameter(description = "조회할 감정 진단 ID", example = "10")
            @PathVariable Long diagnosisId
    ) {
        EmotionDiagnosisDetailResponse response = diagnosisDetailService.getDiagnosis(
                Long.valueOf(principal.getName()), diagnosisId);
        return ResponseEntity.ok(ApiResponse.success("진단 결과를 조회했습니다.", response));
    }
}
