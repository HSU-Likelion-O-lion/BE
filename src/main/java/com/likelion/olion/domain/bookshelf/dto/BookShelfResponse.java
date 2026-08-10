package com.likelion.olion.domain.bookshelf.dto;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;

import java.util.List;

public record BookShelfResponse(List<BookItem> books) {
    public static BookShelfResponse from(List<UserBook> userBooks) {
        return new BookShelfResponse(userBooks.stream().map(BookItem::from).toList());
    }

    public record BookItem(
            Long userBookId,
            BookSummary book,
            String status
    ) {
        private static BookItem from(UserBook userBook) {
            return new BookItem(userBook.getUserBookId(), BookSummary.from(userBook.getBook()),
                    userBook.getStatus().name());
        }
    }

    public record BookSummary(
            Long bookId,
            String title,
            String author,
            String coverImageUrl
    ) {
        private static BookSummary from(Book book) {
            return new BookSummary(book.getBookId(), book.getTitle(), book.getAuthor(), book.getCoverImageUrl());
        }
    }
}
