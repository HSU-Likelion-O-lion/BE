package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityReportRequest;
import com.likelion.olion.domain.community.dto.CommunityReportResponse;
import com.likelion.olion.domain.community.entity.CommunityReportStatus;
import com.likelion.olion.domain.community.service.CommunityReportService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/posts/{postId}/reports")
@Tag(name = "쉼터 게시글 신고", description = "부적절한 쉼터 게시글 신고 API")
public class CommunityReportController {
    private final CommunityReportService communityReportService;

    public CommunityReportController(CommunityReportService communityReportService) {
        this.communityReportService = communityReportService;
    }

    @PostMapping
    @Operation(
            summary = "게시글 신고",
            description = "부적절한 쉼터 게시글을 신고합니다. 같은 사용자는 동일 게시글을 한 번만 신고할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "신고 접수 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 신고한 게시글")
    })
    public ResponseEntity<ApiResponse<CommunityReportResponse>> reportPost(
            Principal principal,
            @Parameter(description = "신고할 게시글 ID", example = "200") @PathVariable Long postId,
            @Valid @RequestBody CommunityReportRequest request
    ) {
        CommunityReportResponse response = communityReportService
                .reportPost(Long.valueOf(principal.getName()), postId, request);
        boolean blinded = response.status() == CommunityReportStatus.BLINDED;
        String code = blinded ? "SUCCESS_BLINDED" : "SUCCESS";
        String message = blinded
                ? "누적 신고 기준을 충족해 게시글이 블라인드 처리되었습니다."
                : "신고가 접수되었습니다.";
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                code, HttpStatus.CREATED, message, response));
    }
}
