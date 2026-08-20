package com.likelion.olion.domain.reflectionshare.controller;

import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareCreateRequest;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareCreateResponse;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareStatusResponse;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareThemeResponse;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.service.ReflectionShareService;
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
@RequestMapping("/api")
@Tag(name = "사유록 공유", description = "사유록 공유 이미지 생성 API")
public class ReflectionShareController {
    private final ReflectionShareService reflectionShareService;

    public ReflectionShareController(ReflectionShareService reflectionShareService) {
        this.reflectionShareService = reflectionShareService;
    }

    @GetMapping("/reflection-shares/themes")
    @Operation(summary = "사유록 공유 테마 조회")
    public ResponseEntity<ApiResponse<ReflectionShareThemeResponse>> getThemes() {
        return ResponseEntity.ok(ApiResponse.success(
                "사유록 공유 테마를 조회했습니다.", reflectionShareService.getThemes()));
    }

    @PostMapping("/reflections/{reflectionId}/shares")
    @Operation(summary = "사유록 공유 이미지 생성 요청")
    public ResponseEntity<ApiResponse<ReflectionShareCreateResponse>> create(
            Principal principal,
            @PathVariable Long reflectionId,
            @Valid @RequestBody ReflectionShareCreateRequest request
    ) {
        ReflectionShareCreateResponse response = reflectionShareService.create(
                Long.valueOf(principal.getName()), reflectionId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(
                "SUCCESS",
                HttpStatus.ACCEPTED,
                "사유록 공유 이미지 생성을 시작했습니다.",
                response));
    }

    @GetMapping("/reflection-shares/{shareId}")
    @Operation(summary = "사유록 공유 이미지 생성 상태 조회")
    public ResponseEntity<ApiResponse<ReflectionShareStatusResponse>> getStatus(
            Principal principal,
            @PathVariable Long shareId
    ) {
        ReflectionShareStatusResponse response = reflectionShareService.getStatus(
                Long.valueOf(principal.getName()), shareId);
        String message = switch (response.status()) {
            case COMPLETED -> "사유록 공유 이미지가 생성되었습니다.";
            case FAILED -> "사유록 공유 이미지 생성에 실패했습니다.";
            case QUEUED, PROCESSING -> "사유록 공유 이미지를 생성하고 있습니다.";
        };
        String code = switch (response.status()) {
            case COMPLETED, FAILED -> "SUCCESS";
            case QUEUED, PROCESSING -> "SUCCESS_PROCESSING";
        };
        return ResponseEntity.ok(ApiResponse.success(code, HttpStatus.OK, message, response));
    }
}
