package com.likelion.olion.domain.mate.dto;

import com.likelion.olion.domain.mate.entity.MatePin;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메이트 핀 고정 도서 목록 응답")
public record MatePinResponse(
        @Schema(description = "핀 고정 도서 목록") List<Pin> pins) {
    public static MatePinResponse from(List<MatePin> matePins) {
        return new MatePinResponse(matePins.stream().map(Pin::from).toList());
    }

    @Schema(description = "핀 고정 도서 항목")
    public record Pin(
            @Schema(description = "책장 도서 항목 ID", example = "30") Long userBookId,
            @Schema(description = "메이트 화면에 표시할 핀 순서", example = "1") Integer pinnedOrder) {
        private static Pin from(MatePin matePin) {
            return new Pin(matePin.getUserBook().getUserBookId(), matePin.getPinnedOrder());
        }
    }
}
