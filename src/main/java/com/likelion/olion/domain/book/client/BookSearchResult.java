package com.likelion.olion.domain.book.client;

public record BookSearchResult(
        String title,
        String author,
        String coverImageUrl,
        String publisher,
        String description,
        String externalUrl,
        String provider,
        String isbn13,
        String providerBookId,
        String category
) {
    public BookSearchResult {
        externalUrl = blankToNull(externalUrl);
        isbn13 = blankToNull(isbn13);
        providerBookId = blankToNull(providerBookId);
        category = blankToNull(category);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
