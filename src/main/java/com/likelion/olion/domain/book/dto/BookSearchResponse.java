package com.likelion.olion.domain.book.dto;

import com.likelion.olion.domain.book.entity.Book;

import java.util.List;

public record BookSearchResponse(List<BookSummary> books) {
    public static BookSearchResponse from(List<Book> books) {
        return new BookSearchResponse(books.stream()
                .map(BookSummary::from)
                .toList());
    }

    public record BookSummary(
            Long bookId,
            String title,
            String author,
            String coverImageUrl
    ) {
        private static BookSummary from(Book book) {
            return new BookSummary(
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCoverImageUrl()
            );
        }
    }
}
