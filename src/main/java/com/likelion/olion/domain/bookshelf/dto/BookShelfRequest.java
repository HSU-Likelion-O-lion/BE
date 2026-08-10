package com.likelion.olion.domain.bookshelf.dto;

import jakarta.validation.constraints.NotNull;

public record BookShelfRequest(@NotNull Long bookId) {
}
