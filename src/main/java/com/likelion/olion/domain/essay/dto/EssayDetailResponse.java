package com.likelion.olion.domain.essay.dto;

import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "에세이 상세 응답")
public record EssayDetailResponse(
        @Schema(description = "에세이 ID", example = "7") Long essayId,
        @Schema(description = "제목 (발행 전이면 null)", example = "흔들려도 걷는 마음") String title,
        @Schema(description = "작업 상태", example = "COMPLETED") EssayStatus status,
        @Schema(description = "발행 시각 (발행 전이면 null)", example = "2026-08-11T09:00:00Z") Instant publishedAt,
        @Schema(description = "생성 시각", example = "2026-08-10T09:00:00Z") Instant createdAt,
        @Schema(description = "본문 (장 목록)") List<Chapter> chapters
) {
    public static EssayDetailResponse of(Essay essay, List<Chapter> chapters) {
        return new EssayDetailResponse(
                essay.getEssayId(), essay.getTitle(), essay.getStatus(),
                essay.getPublishedAt(), essay.getCreatedAt(), chapters);
    }

    @Schema(description = "본문 한 장")
    public record Chapter(
            @Schema(description = "장 번호", example = "1") Integer chapterNo,
            @Schema(description = "장 제목", example = "1장") String title,
            @Schema(description = "해당 장에 속한 사유 본문 목록") List<String> reflections
    ) {
    }
}
