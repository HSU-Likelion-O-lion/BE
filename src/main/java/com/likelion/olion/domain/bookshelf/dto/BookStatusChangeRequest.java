package com.likelion.olion.domain.bookshelf.dto;

import jakarta.validation.constraints.NotBlank;

public record BookStatusChangeRequest(@NotBlank String status) {
}
