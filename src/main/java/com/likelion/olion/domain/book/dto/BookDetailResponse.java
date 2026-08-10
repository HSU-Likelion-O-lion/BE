package com.likelion.olion.domain.book.dto;

import com.likelion.olion.domain.book.entity.Book;

public record BookDetailResponse(
        Long bookId,
        String title,
        String author,
        String coverImageUrl,
        String publisher,
        String description,
        String externalUrl,
        String provider
) {
    public static BookDetailResponse from(Book book) {
        return new BookDetailResponse(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImageUrl(),
                book.getPublisher(),
                book.getDescription(),
                book.getExternalUrl(),
                book.getProvider()
        );
    }
}
