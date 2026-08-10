package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 공감 상태")
public record CommunityHeartResponse(
        @Schema(description = "현재 사용자의 공감 여부", example = "true")
        boolean isHearted
) {
}
