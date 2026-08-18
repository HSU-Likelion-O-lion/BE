package com.likelion.olion.domain.essay.controller;

import com.likelion.olion.domain.essay.dto.EssayCreateRequest;
import com.likelion.olion.domain.essay.dto.EssayCreateResponse;
import com.likelion.olion.domain.essay.dto.EssayDetailResponse;
import com.likelion.olion.domain.essay.dto.EssayDraftResponse;
import com.likelion.olion.domain.essay.dto.EssayJobStatusResponse;
import com.likelion.olion.domain.essay.dto.EssayListResponse;
import com.likelion.olion.domain.essay.dto.EssayPublishRequest;
import com.likelion.olion.domain.essay.dto.EssayPublishResponse;
import com.likelion.olion.domain.essay.service.EssayService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @Operation(summary = "에세이 생성 요청", description = "선택한 사유 30개로 2,000~2,500자 내외의 1인칭 회고 에세이 생성을 비동기로 요청합니다.")
    public ResponseEntity<ApiResponse<EssayCreateResponse>> create(
            Principal principal,
            @Valid @RequestBody EssayCreateRequest request
    ) {
        EssayCreateResponse response = essayService.create(Long.valueOf(principal.getName()), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("SUCCESS", HttpStatus.ACCEPTED, "에세이 편집 작업이 접수되었습니다.", response));
    }

    @GetMapping
    @Operation(summary = "내 에세이 목록 조회", description = "로그인한 사용자의 에세이 목록을 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<EssayListResponse>> getList(Principal principal) {
        EssayListResponse response = essayService.getList(Long.valueOf(principal.getName()));
        return ResponseEntity.ok(ApiResponse.success("에세이 목록을 조회했습니다.", response));
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
    @Operation(summary = "실패한 작업 재생성", description = "FAILED 상태의 에세이를 다시 생성합니다. 사용자별 하루 1회로 제한됩니다.")
    public ResponseEntity<ApiResponse<EssayJobStatusResponse>> retry(
            Principal principal,
            @PathVariable Long essayId
    ) {
        EssayJobStatusResponse response = essayService.retry(Long.valueOf(principal.getName()), essayId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("SUCCESS", HttpStatus.ACCEPTED, "에세이 편집을 다시 시도합니다.", response));
    }

    @PostMapping("/{essayId}/cancel")
    @Operation(summary = "에세이 작업 취소", description = "대기 중이거나 처리 중인 AI 에세이 편집 작업을 취소합니다.")
    public ResponseEntity<ApiResponse<EssayJobStatusResponse>> cancel(
            Principal principal,
            @PathVariable Long essayId
    ) {
        EssayJobStatusResponse response = essayService.cancel(Long.valueOf(principal.getName()), essayId);
        return ResponseEntity.ok(ApiResponse.success("에세이 편집 작업을 취소했습니다.", response));
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

    @GetMapping("/{essayId}")
    @Operation(summary = "에세이 상세 조회", description = "발행되었거나 편집이 완료된 에세이의 본문(장별 사유 내용)을 조회합니다.")
    public ResponseEntity<ApiResponse<EssayDetailResponse>> getDetail(
            Principal principal,
            @PathVariable Long essayId
    ) {
        EssayDetailResponse response = essayService.getDetail(Long.valueOf(principal.getName()), essayId);
        return ResponseEntity.ok(ApiResponse.success("에세이 상세를 조회했습니다.", response));
    }

    @GetMapping("/{essayId}/download")
    @Operation(summary = "에세이 PDF 다운로드", description = "발행되었거나 편집이 완료된 에세이를 PDF 파일로 다운로드합니다.")
    public ResponseEntity<byte[]> download(
            Principal principal,
            @PathVariable Long essayId
    ) {
        byte[] pdf = essayService.downloadPdf(Long.valueOf(principal.getName()), essayId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("essay-" + essayId + ".pdf")
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }

    @PostMapping("/{essayId}/publish")
    @Operation(summary = "제목 확정 및 발행", description = "책 제목을 입력하고 에세이를 최종 발행합니다.")
    public ResponseEntity<ApiResponse<EssayPublishResponse>> publish(
            Principal principal,
            @PathVariable Long essayId,
            @Valid @RequestBody EssayPublishRequest request
    ) {
        EssayPublishResponse response = essayService.publish(Long.valueOf(principal.getName()), essayId, request);
        return ResponseEntity.ok(ApiResponse.success("에세이가 발행되었습니다.", response));
    }
}
