package com.likelion.olion.domain.essay.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "에세이 초안(목차) 응답")
public record EssayDraftResponse(
        @Schema(description = "AI가 생성한 에세이 제목", example = "흔들려도 걷는 마음") String title,
        @Schema(description = "목차 목록") List<Chapter> chapters
) {
    public EssayDraftResponse(List<Chapter> chapters) {
        this(null, chapters);
    }
    @Schema(description = "목차 한 장")
    public record Chapter(
            @Schema(description = "장 번호", example = "1") Integer chapterNo,
            @Schema(description = "장 제목", example = "1장") String title,
            @Schema(description = "AI가 생성한 장 본문") String content,
            @Schema(description = "해당 장에 속한 사유 ID 목록") List<Long> reflectionIds
    ) {
        public Chapter(Integer chapterNo, String title, List<Long> reflectionIds) {
            this(chapterNo, title, null, reflectionIds);
        }
    }
}
