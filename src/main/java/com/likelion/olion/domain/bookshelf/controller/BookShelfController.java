package com.likelion.olion.domain.bookshelf.controller;

import com.likelion.olion.domain.bookshelf.dto.BookShelfRequest;
import com.likelion.olion.domain.bookshelf.dto.BookShelfResponse;
import com.likelion.olion.domain.bookshelf.dto.BookShelfSaveResponse;
import com.likelion.olion.domain.bookshelf.dto.BookStatusChangeRequest;
import com.likelion.olion.domain.bookshelf.service.BookShelfService;
import com.likelion.olion.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/bookshelf")
public class BookShelfController {
    private final BookShelfService bookShelfService;

    public BookShelfController(BookShelfService bookShelfService) {
        this.bookShelfService = bookShelfService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BookShelfResponse>> getBookshelf(
            Principal principal,
            @RequestParam(required = false) String status
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                "책장 목록을 조회했습니다.", bookShelfService.getBookshelf(userId, status)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookShelfSaveResponse>> addBook(
            Principal principal,
            @Valid @RequestBody BookShelfRequest request
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "책장에 담았습니다.", bookShelfService.addBook(userId, request)));
    }

    @PatchMapping("/{userBookId}")
    public ResponseEntity<ApiResponse<BookShelfSaveResponse>> changeStatus(
            Principal principal,
            @PathVariable Long userBookId,
            @Valid @RequestBody BookStatusChangeRequest request
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                "도서 상태가 변경되었습니다.",
                bookShelfService.changeStatus(userId, userBookId, request.status())));
    }

    @DeleteMapping("/{userBookId}")
    public ResponseEntity<Void> deleteBook(Principal principal, @PathVariable Long userBookId) {
        bookShelfService.deleteBook(getUserId(principal), userBookId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
