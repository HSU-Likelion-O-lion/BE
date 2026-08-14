package com.likelion.olion.domain.book.controller;

import com.likelion.olion.domain.book.dto.BookCurationResponse;
import com.likelion.olion.domain.book.service.BookCurationService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/books")
@Tag(name = "도서", description = "도서 상세, 검색 및 감정 맞춤 소개글 API")
public class BookCurationController {
    private final BookCurationService curationService;

    public BookCurationController(BookCurationService curationService) {
        this.curationService = curationService;
    }

    @GetMapping("/{bookId}/curation")
    @Operation(
            summary = "도서 맞춤 소개글 조회",
            description = "본인의 감정 진단 결과에 맞춘 도서 소개 문구를 조회합니다. 생성된 문구는 재사용됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "맞춤 소개글 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "다른 사용자의 진단 결과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "도서 또는 진단 결과를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<BookCurationResponse>> getCuration(
            Principal principal,
            @Parameter(description = "맞춤 소개글을 조회할 도서 ID", example = "5")
            @PathVariable Long bookId,
            @Parameter(description = "소개글 생성에 사용할 감정 진단 ID", example = "10")
            @RequestParam Long diagnosisId
    ) {
        BookCurationResponse response = curationService.getCuration(
                Long.valueOf(principal.getName()), bookId, diagnosisId);
        return ResponseEntity.ok(ApiResponse.success("소개 글귀를 조회했습니다.", response));
    }
}
