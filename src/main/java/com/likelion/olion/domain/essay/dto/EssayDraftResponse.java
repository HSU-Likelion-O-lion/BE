package com.likelion.olion.domain.essay.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "에세이 초안(목차) 응답")
public record EssayDraftResponse(
        @Schema(description = "목차 목록") List<Chapter> chapters
) {
    @Schema(description = "목차 한 장")
    public record Chapter(
            @Schema(description = "장 번호", example = "1") Integer chapterNo,
            @Schema(description = "장 제목", example = "1장") String title,
            @Schema(description = "해당 장에 속한 사유 ID 목록") List<Long> reflectionIds
    ) {
    }
}
