package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "본인 게시글 수정 요청")
public record CommunityPostUpdateRequest(
        @NotBlank
        @Schema(
                description = "수정할 게시글 본문",
                example = "오늘따라 유독 마음이 오래 남았습니다.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String content
) {
}
