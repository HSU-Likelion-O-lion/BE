package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "공유 이미지 생성 요청")
public record CommunityShareCreateRequest(
        @NotNull
        @Positive
        @Schema(description = "공유 이미지에 적용할 테마 ID", example = "2")
        Long themeId
) {
}
