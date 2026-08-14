package com.likelion.olion.domain.book.dto;

import com.likelion.olion.domain.book.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도서 상세 응답")
public record BookDetailResponse(
        @Schema(description = "도서 ID", example = "1")
        Long bookId,
        @Schema(description = "도서 제목", example = "어린 왕자")
        String title,
        @Schema(description = "저자", example = "앙투안 드 생텍쥐페리")
        String author,
        @Schema(description = "표지 이미지 URL")
        String coverImageUrl,
        @Schema(description = "출판사", example = "열린책들")
        String publisher,
        @Schema(description = "도서 설명")
        String description,
        @Schema(description = "외부 도서 상세 URL")
        String externalUrl,
        @Schema(description = "도서 정보 제공자", example = "KAKAO")
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
