package com.likelion.olion.domain.essay.controller;

import com.likelion.olion.domain.essay.dto.EssayCreateRequest;
import com.likelion.olion.domain.essay.dto.EssayCreateResponse;
import com.likelion.olion.domain.essay.dto.EssayDraftResponse;
import com.likelion.olion.domain.essay.dto.EssayJobStatusResponse;
import com.likelion.olion.domain.essay.service.EssayService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/essays")
@Tag(name = "에세이", description = "사유를 엮은 에세이 출판 API")
public class EssayController {
    private final EssayService essayService;

    public EssayController(EssayService essayService) {
        this.essayService = essayService;
    }

    @PostMapping
    @Operation(summary = "에세이 생성 요청", description = "선택한 사유들로 AI 편집자에게 에세이 목차 구성을 비동기로 요청합니다.")
    public ResponseEntity<ApiResponse<EssayCreateResponse>> create(
            Principal principal,
            @Valid @RequestBody EssayCreateRequest request
    ) {
        EssayCreateResponse response = essayService.create(Long.valueOf(principal.getName()), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("SUCCESS", HttpStatus.ACCEPTED, "에세이 편집 작업이 접수되었습니다.", response));
    }

    @GetMapping("/{essayId}/job-status")
    @Operation(summary = "에세이 작업 상태 조회", description = "AI 편집자의 에세이 목차 구성 작업 진행 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<EssayJobStatusResponse>> getJobStatus(
            Principal principal,
            @PathVariable Long essayId
    ) {
        EssayJobStatusResponse response = essayService.getJobStatus(Long.valueOf(principal.getName()), essayId);
        return ResponseEntity.ok(ApiResponse.success("작업 상태를 조회했습니다.", response));
    }

    @PostMapping("/{essayId}/retry")
    @Operation(summary = "실패한 작업 재시도", description = "FAILED 상태의 에세이 생성 작업을 다시 시도합니다.")
    public ResponseEntity<ApiResponse<EssayJobStatusResponse>> retry(
            Principal principal,
            @PathVariable Long essayId
    ) {
        EssayJobStatusResponse response = essayService.retry(Long.valueOf(principal.getName()), essayId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("SUCCESS", HttpStatus.ACCEPTED, "에세이 편집을 다시 시도합니다.", response));
    }

    @GetMapping("/{essayId}/draft")
    @Operation(summary = "에세이 초안 조회", description = "AI 편집자가 구성한 에세이 목차 초안을 조회합니다.")
    public ResponseEntity<ApiResponse<EssayDraftResponse>> getDraft(
            Principal principal,
            @PathVariable Long essayId
    ) {
        EssayDraftResponse response = essayService.getDraft(Long.valueOf(principal.getName()), essayId);
        return ResponseEntity.ok(ApiResponse.success("에세이 초안을 조회했습니다.", response));
    }
}
