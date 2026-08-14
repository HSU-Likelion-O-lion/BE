package com.likelion.olion.domain.essay.dto;

import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "에세이 목록 응답")
public record EssayListResponse(
        @Schema(description = "에세이 목록 (최신순)") List<Item> essays
) {
    public static EssayListResponse of(List<Essay> essays) {
        return new EssayListResponse(essays.stream().map(Item::from).toList());
    }

    @Schema(description = "에세이 항목")
    public record Item(
            @Schema(description = "에세이 ID", example = "7") Long essayId,
            @Schema(description = "제목 (발행 전이면 null)", example = "흔들려도 걷는 마음") String title,
            @Schema(description = "에세이 저자명", example = "책을 사랑하는 사자") String authorName,
            @Schema(description = "작업 상태", example = "COMPLETED") EssayStatus status,
            @Schema(description = "발행 시각 (발행 전이면 null)", example = "2026-08-11T09:00:00Z") Instant publishedAt,
            @Schema(description = "생성 시각", example = "2026-08-10T09:00:00Z") Instant createdAt
    ) {
        private static Item from(Essay essay) {
            return new Item(
                    essay.getEssayId(), essay.getTitle(), essay.getAuthorName(), essay.getStatus(),
                    essay.getPublishedAt(), essay.getCreatedAt());
        }
    }
}
