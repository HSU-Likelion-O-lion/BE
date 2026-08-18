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
        isbn13 = normalizeIsbn13(isbn13);
        providerBookId = blankToNull(providerBookId);
        category = blankToNull(category);
    }

    public boolean hasMissingMetadata() {
        return isBlank(title) || isBlank(author) || isBlank(coverImageUrl)
                || isBlank(publisher) || isBlank(description);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String normalizeIsbn13(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replaceAll("[^0-9]", "");
        return normalized.length() == 13 ? normalized : null;
    }
}
