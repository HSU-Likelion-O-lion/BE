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

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(
                "도서 상세 정보를 조회했습니다.",
                bookService.getBook(bookId)
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BookSearchResponse>> searchBooks(
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
