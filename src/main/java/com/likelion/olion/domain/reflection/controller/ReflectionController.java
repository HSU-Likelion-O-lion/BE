package com.likelion.olion.domain.reflection.controller;

import com.likelion.olion.domain.reflection.dto.ReflectionCreateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionDeleteResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionListResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionUpdateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionUpdateResponse;
import com.likelion.olion.domain.reflection.service.ReflectionService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/reflections")
@Tag(name = "사유", description = "서재 사유록 API")
public class ReflectionController {
    private final ReflectionService reflectionService;

    public ReflectionController(ReflectionService reflectionService) {
        this.reflectionService = reflectionService;
    }

    @PostMapping
    @Operation(summary = "사유 작성", description = "독서 세션의 AI 질문에 답하는 사유를 작성합니다.")
    public ResponseEntity<ApiResponse<ReflectionCreateResponse>> create(
            Principal principal,
            @Valid @RequestBody ReflectionCreateRequest request
    ) {
        ReflectionCreateResponse response = reflectionService.create(Long.valueOf(principal.getName()), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SUCCESS", HttpStatus.CREATED, "사유가 저장되었습니다.", response));
    }

    @GetMapping
    @Operation(summary = "사유 목록 조회", description = "서재에 쌓인 사유 목록과 표지 진행도를 조회합니다.")
    public ResponseEntity<ApiResponse<ReflectionListResponse>> getList(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "사유 목록을 조회했습니다.",
                reflectionService.getList(Long.valueOf(principal.getName()))));
    }

    @PatchMapping("/{reflectionId}")
    @Operation(summary = "사유 수정", description = "본인이 작성한 사유의 내용을 수정합니다.")
    public ResponseEntity<ApiResponse<ReflectionUpdateResponse>> update(
            Principal principal,
            @PathVariable Long reflectionId,
            @Valid @RequestBody ReflectionUpdateRequest request
    ) {
        ReflectionUpdateResponse response = reflectionService.update(
                Long.valueOf(principal.getName()), reflectionId, request);
        return ResponseEntity.ok(ApiResponse.success("사유가 수정되었습니다.", response));
    }

    @DeleteMapping("/{reflectionId}")
    @Operation(summary = "사유 삭제", description = "본인이 작성한 사유를 삭제합니다.")
    public ResponseEntity<ApiResponse<ReflectionDeleteResponse>> delete(
            Principal principal,
            @PathVariable Long reflectionId
    ) {
        ReflectionDeleteResponse response = reflectionService.delete(Long.valueOf(principal.getName()), reflectionId);
        return ResponseEntity.ok(ApiResponse.success("사유가 삭제되었습니다.", response));
    }
}
