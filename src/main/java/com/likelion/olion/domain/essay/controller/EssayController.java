package com.likelion.olion.domain.essay.controller;

import com.likelion.olion.domain.essay.dto.EssayCreateRequest;
import com.likelion.olion.domain.essay.dto.EssayCreateResponse;
import com.likelion.olion.domain.essay.service.EssayService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
