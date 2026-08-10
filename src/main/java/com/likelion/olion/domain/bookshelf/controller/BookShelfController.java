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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/bookshelf")
@Tag(name = "책장", description = "사용자 책장에 도서를 등록하고 독서 상태를 관리하는 API")
public class BookShelfController {
    private final BookShelfService bookShelfService;

    public BookShelfController(BookShelfService bookShelfService) {
        this.bookShelfService = bookShelfService;
    }

    @GetMapping
    @Operation(summary = "책장 도서 목록 조회", description = "로그인한 사용자의 책장 도서를 조회합니다. status를 생략하면 전체 도서를 반환합니다.")
    public ResponseEntity<ApiResponse<BookShelfResponse>> getBookshelf(
            Principal principal,
            @Parameter(description = "필터링할 독서 상태", example = "READING", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"BEFORE_READING", "READING", "DONE"}))
            @RequestParam(required = false) String status
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                "책장 목록을 조회했습니다.", bookShelfService.getBookshelf(userId, status)));
    }

    @PostMapping
    @Operation(summary = "책장에 도서 등록", description = "도서 ID를 사용자의 책장에 등록합니다. 등록 직후 상태는 BEFORE_READING입니다.")
    public ResponseEntity<ApiResponse<BookShelfSaveResponse>> addBook(
            Principal principal,
            @Valid @RequestBody BookShelfRequest request
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "책장에 담았습니다.", bookShelfService.addBook(userId, request)));
    }

    @PatchMapping("/{userBookId}")
    @Operation(summary = "책장 도서 상태 변경", description = "책장에 등록된 도서의 독서 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<BookShelfSaveResponse>> changeStatus(
            Principal principal,
            @Parameter(description = "책장 도서 항목 ID", example = "30")
            @PathVariable Long userBookId,
            @Valid @RequestBody BookStatusChangeRequest request
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                "도서 상태가 변경되었습니다.",
                bookShelfService.changeStatus(userId, userBookId, request.status())));
    }

    @DeleteMapping("/{userBookId}")
    @Operation(summary = "책장 도서 삭제", description = "로그인한 사용자의 책장에서 도서를 삭제합니다.")
    public ResponseEntity<Void> deleteBook(Principal principal,
            @Parameter(description = "책장 도서 항목 ID", example = "30") @PathVariable Long userBookId) {
        bookShelfService.deleteBook(getUserId(principal), userBookId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
