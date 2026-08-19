package com.likelion.olion.domain.bookshelf.dto;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.bookshelf.entity.UserBook;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "책장 도서 목록 응답")
public record BookShelfResponse(
        @Schema(description = "책장 도서 목록") List<BookItem> books) {
    public static BookShelfResponse from(List<UserBook> userBooks) {
        return new BookShelfResponse(userBooks.stream().map(BookItem::from).toList());
    }

    @Schema(description = "책장 도서 항목")
    public record BookItem(
            @Schema(description = "책장 도서 항목 ID", example = "30")
            Long userBookId,
            @Schema(description = "도서 요약 정보")
            BookSummary book,
            @Schema(description = "독서 상태", example = "READING")
            String status
    ) {
        private static BookItem from(UserBook userBook) {
            return new BookItem(userBook.getUserBookId(), BookSummary.from(userBook.getBook()),
                    userBook.getStatus().name());
        }
    }

    @Schema(description = "책장 도서 요약 정보")
    public record BookSummary(
            @Schema(description = "도서 ID", example = "1")
            Long bookId,
            @Schema(description = "도서 제목", example = "어린 왕자")
            String title,
            @Schema(description = "저자", example = "앙투안 드 생텍쥐페리")
            String author,
            @Schema(description = "표지 이미지 URL")
            String coverImageUrl,
            @Schema(description = "출판사", example = "열린책들")
            String publisher
    ) {
        private static BookSummary from(Book book) {
            return new BookSummary(
                    book.getBookId(), book.getTitle(), book.getAuthor(),
                    book.getCoverImageUrl(), book.getPublisher());
        }
    }
}
