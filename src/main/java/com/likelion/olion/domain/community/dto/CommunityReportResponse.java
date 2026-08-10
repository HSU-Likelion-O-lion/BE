package com.likelion.olion.domain.community.dto;

import com.likelion.olion.domain.community.entity.CommunityReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 신고 접수 결과")
public record CommunityReportResponse(
        @Schema(description = "생성된 신고 ID", example = "9")
        Long reportId,

        @Schema(description = "게시글 신고 처리 상태", example = "NORMAL")
        CommunityReportStatus status
) {
}
