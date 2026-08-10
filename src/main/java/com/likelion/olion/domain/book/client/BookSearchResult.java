package com.likelion.olion.domain.book.client;

public record BookSearchResult(
        String title,
        String author,
        String coverImageUrl,
        String publisher,
        String description,
        String externalUrl,
        String provider
) {
}
