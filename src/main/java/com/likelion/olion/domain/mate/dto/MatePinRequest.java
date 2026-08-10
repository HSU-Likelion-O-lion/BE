package com.likelion.olion.domain.mate.dto;

import jakarta.validation.constraints.NotNull;

public record MatePinRequest(@NotNull Long userBookId) {
}
