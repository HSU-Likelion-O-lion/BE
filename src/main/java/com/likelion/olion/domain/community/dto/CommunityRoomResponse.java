package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "책별 쉼터 목록 응답")
public record CommunityRoomResponse(
        @Schema(description = "소통방 목록") List<Room> rooms
) {
    @Schema(description = "소통방 정보")
    public record Room(
            @Schema(description = "소통방 ID", example = "12") Long roomId,
            @Schema(description = "연결된 도서 ID", example = "5") Long bookId,
            @Schema(description = "연결된 도서 제목", example = "아몬드") String bookTitle
    ) {
    }
}
