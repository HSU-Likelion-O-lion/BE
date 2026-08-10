package com.likelion.olion.domain.book.controller;

import com.likelion.olion.domain.book.dto.BookDetailResponse;
import com.likelion.olion.domain.book.dto.BookSearchResponse;
import com.likelion.olion.domain.book.service.BookService;
import com.likelion.olion.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/books")
@Tag(name = "도서", description = "도서 상세 조회와 외부 도서 검색 API")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{bookId}")
    @Operation(summary = "도서 상세 조회", description = "도서 ID로 도서의 상세 정보와 외부 제공자 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBook(
            @Parameter(description = "조회할 도서 ID", example = "1") @PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(
                "도서 상세 정보를 조회했습니다.",
                bookService.getBook(bookId)
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "도서 검색", description = "카카오와 알라딘 Open API를 우선 사용하고, 실패 시 내부 도서 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<BookSearchResponse>> searchBooks(
            @Parameter(description = "검색할 도서명, 저자 또는 키워드", example = "어린 왕자")
            @RequestParam(name = "q", required = false) String query
    ) {
        BookSearchResponse response = bookService.searchBooks(query);
        String message = response.books().isEmpty()
                ? "검색 결과가 없습니다."
                : "검색 결과를 조회했습니다.";
        String code = response.books().isEmpty() ? "SUCCESS_EMPTY" : "SUCCESS";

        return ResponseEntity.ok(ApiResponse.success(
                code,
                org.springframework.http.HttpStatus.OK,
                message,
                response
        ));
    }
}
