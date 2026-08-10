package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쉼터 입장 가능 여부 응답")
public record CommunityAccessResponse(
        @Schema(description = "오늘 독서 목표 달성에 따른 쉼터 입장 가능 여부", example = "true")
        boolean canEnter
) {
}
