package com.likelion.olion.domain.book.dto;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.client.BookSearchResult;

import java.util.List;

public record BookSearchResponse(List<BookSummary> books) {
    public static BookSearchResponse from(List<Book> books) {
        return new BookSearchResponse(books.stream()
                .map(BookSummary::from)
                .toList());
    }

    public static BookSearchResponse fromExternal(List<BookSearchResult> books) {
        return new BookSearchResponse(books.stream()
                .map(book -> new BookSummary(
                        null,
                        book.title(),
                        book.author(),
                        book.coverImageUrl()
                ))
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
