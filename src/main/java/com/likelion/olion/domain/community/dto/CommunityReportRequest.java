package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 신고 요청")
public record CommunityReportRequest(
        @Size(max = 500)
        @Schema(description = "신고 사유. 생략할 수 있습니다.", example = "부적절한 표현", nullable = true)
        String reason
) {
}
