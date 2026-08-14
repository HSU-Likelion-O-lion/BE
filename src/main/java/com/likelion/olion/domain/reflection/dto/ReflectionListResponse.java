package com.likelion.olion.domain.reflection.dto;

import com.likelion.olion.domain.reflection.entity.Reflection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "사유 목록 응답")
public record ReflectionListResponse(
        @Schema(description = "표지 진행도 (0~30)", example = "12") int coverProgress,
        @Schema(description = "사유 목록 (최신순)") List<Item> reflections
) {
    public static ReflectionListResponse of(int coverProgress, List<Reflection> reflections) {
        return new ReflectionListResponse(coverProgress, reflections.stream().map(Item::from).toList());
    }

    @Schema(description = "사유 항목")
    public record Item(
            @Schema(description = "사유 ID", example = "88") Long reflectionId,
            @Schema(description = "본문", example = "오늘 읽은 부분에서...") String content,
            @Schema(description = "작성 시각", example = "2026-08-10T09:00:00Z") Instant createdAt
    ) {
        private static Item from(Reflection reflection) {
            return new Item(reflection.getReflectionId(), reflection.getContent(), reflection.getCreatedAt());
        }
    }
}
