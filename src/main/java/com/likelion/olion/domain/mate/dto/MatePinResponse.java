package com.likelion.olion.domain.mate.dto;

import com.likelion.olion.domain.mate.entity.MatePin;

import java.util.List;

public record MatePinResponse(List<Pin> pins) {
    public static MatePinResponse from(List<MatePin> matePins) {
        return new MatePinResponse(matePins.stream().map(Pin::from).toList());
    }

    public record Pin(Long userBookId, Integer pinnedOrder) {
        private static Pin from(MatePin matePin) {
            return new Pin(matePin.getUserBook().getUserBookId(), matePin.getPinnedOrder());
        }
    }
}
