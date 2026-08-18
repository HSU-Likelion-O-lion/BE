package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.client.BookSearchResult;

import java.util.List;

public final class BookMetadataResolver {
    private BookMetadataResolver() {
    }

    public static List<BookSearchResult> merge(
            List<BookSearchResult> primaryResults,
            List<BookSearchResult> supplementResults
    ) {
        return primaryResults.stream()
                .map(primary -> supplementResults.stream()
                        .filter(supplement -> sameBook(primary, supplement))
                        .findFirst()
                        .map(supplement -> merge(primary, supplement))
                        .orElse(primary))
                .toList();
    }

    private static BookSearchResult merge(BookSearchResult primary, BookSearchResult supplement) {
        return new BookSearchResult(
                prefer(primary.title(), supplement.title()),
                prefer(primary.author(), supplement.author()),
                prefer(primary.coverImageUrl(), supplement.coverImageUrl()),
                prefer(primary.publisher(), supplement.publisher()),
                prefer(primary.description(), supplement.description()),
                prefer(primary.externalUrl(), supplement.externalUrl()),
                primary.provider(),
                prefer(primary.isbn13(), supplement.isbn13()),
                prefer(primary.providerBookId(), supplement.providerBookId()),
                prefer(primary.category(), supplement.category())
        );
    }

    private static boolean sameBook(BookSearchResult primary, BookSearchResult supplement) {
        return hasText(primary.isbn13()) && hasText(supplement.isbn13())
                && normalizeIsbn(primary.isbn13()).equals(normalizeIsbn(supplement.isbn13()));
    }

    private static String prefer(String primary, String supplement) {
        return hasText(primary) ? primary : supplement;
    }

    private static String normalizeIsbn(String value) {
        return value.replaceAll("[^0-9Xx]", "").toUpperCase();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
