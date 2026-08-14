package com.likelion.olion.domain.reflection.dto;

import com.likelion.olion.domain.reflection.entity.Reflection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "출판 가능한 사유 조회 응답")
public record ReflectionPublishableResponse(
        @Schema(description = "출판 가능 여부", example = "false") boolean canPublish,
        @Schema(description = "부족한 사유 개수 (출판 가능하면 null)", example = "5", nullable = true) Integer needed,
        @Schema(description = "선택 가능한 사유 목록 (출판 가능할 때만 값 존재)") List<Item> reflections
) {
    public static ReflectionPublishableResponse notEnough(int needed) {
        return new ReflectionPublishableResponse(false, needed, List.of());
    }

    public static ReflectionPublishableResponse ready(List<Reflection> reflections) {
        return new ReflectionPublishableResponse(true, null, reflections.stream().map(Item::from).toList());
    }

    @Schema(description = "선택 가능한 사유")
    public record Item(
            @Schema(description = "사유 ID", example = "88") Long reflectionId,
            @Schema(description = "본문", example = "오늘 읽은 부분에서...") String content
    ) {
        private static Item from(Reflection reflection) {
            return new Item(reflection.getReflectionId(), reflection.getContent());
        }
    }
}
