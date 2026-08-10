package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityShareStatusResponse;
import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import com.likelion.olion.domain.community.service.CommunityShareService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/shares")
@Tag(name = "쉼터 공유", description = "쉼터 게시글 외부 공유 이미지 API")
public class CommunityShareQueryController {
    private final CommunityShareService communityShareService;

    public CommunityShareQueryController(CommunityShareService communityShareService) {
        this.communityShareService = communityShareService;
    }

    @GetMapping("/{shareId}")
    @Operation(
            summary = "공유 이미지 생성 상태 조회",
            description = "공유 이미지 생성 작업의 진행 상태와 완료된 이미지 URL을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "QUEUED, PROCESSING 또는 COMPLETED 상태 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "작업을 찾을 수 없거나 접근할 수 없음")
    })
    public ResponseEntity<ApiResponse<CommunityShareStatusResponse>> getShareStatus(
            Principal principal,
            @Parameter(description = "공유 이미지 생성 작업 ID", example = "30")
            @PathVariable Long shareId
    ) {
        CommunityShareStatusResponse response = communityShareService.getShareStatus(
                Long.valueOf(principal.getName()), shareId);
        boolean completed = response.status() == CommunityShareStatus.COMPLETED;
        String code = completed ? "SUCCESS" : "SUCCESS_PROCESSING";
        String message = completed ? "공유 이미지가 생성되었습니다." : "이미지 생성 중입니다.";
        return ResponseEntity.ok(ApiResponse.success(code, HttpStatus.OK, message, response));
    }
}
