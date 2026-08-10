package com.likelion.olion.domain.book.dto;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.client.BookSearchResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "도서 검색 응답")
public record BookSearchResponse(
        @Schema(description = "검색된 도서 목록") List<BookSummary> books) {
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

    @Schema(description = "검색 도서 요약 정보")
    public record BookSummary(
            @Schema(description = "내부 도서 ID. 외부 검색 결과는 저장 전이면 null일 수 있습니다.", example = "1")
            Long bookId,
            @Schema(description = "도서 제목", example = "어린 왕자")
            String title,
            @Schema(description = "저자", example = "앙투안 드 생텍쥐페리")
            String author,
            @Schema(description = "표지 이미지 URL")
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
