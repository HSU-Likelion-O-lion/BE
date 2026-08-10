package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "공유 배경 테마 목록")
public record CommunityShareThemeResponse(
        @Schema(description = "공유 배경 테마 배열")
        List<Theme> themes
) {
    @Schema(description = "공유 배경 테마")
    public record Theme(
            @Schema(description = "테마 ID", example = "2")
            Long themeId,

            @Schema(description = "테마 이름", example = "밤하늘")
            String name,

            @Schema(description = "테마 미리보기 이미지 URL", example = "https://cdn.olion.com/theme/2.png")
            String previewUrl
    ) {
    }
}
