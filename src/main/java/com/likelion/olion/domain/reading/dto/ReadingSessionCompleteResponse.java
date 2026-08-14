package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 완료 결과")
public record ReadingSessionCompleteResponse(
        @Schema(description = "완료된 세션 상태", example = "COMPLETED")
        String status,
        @Schema(description = "독서 후 기록을 위한 질문", example = "오늘 읽은 부분에서 가장 마음에 남는 문장은?")
        String aiQuestion
) {
}
