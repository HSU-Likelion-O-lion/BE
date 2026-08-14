package com.likelion.olion.domain.book.controller;

import com.likelion.olion.domain.book.dto.BookPurchaseClickResponse;
import com.likelion.olion.domain.book.service.BookPurchaseClickService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/books")
@Tag(name = "도서", description = "도서 상세, 검색, 큐레이션 및 구매 링크 API")
public class BookPurchaseClickController {
    private final BookPurchaseClickService purchaseClickService;

    public BookPurchaseClickController(BookPurchaseClickService purchaseClickService) {
        this.purchaseClickService = purchaseClickService;
    }

    @PostMapping("/{bookId}/purchase-click")
    @Operation(
            summary = "도서 구매 링크 클릭 기록",
            description = "외부 서점으로 이동하는 클릭을 기록하고 안전한 구매 링크를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "클릭 기록 및 이동 링크 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "도서를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "도서에 유효한 구매 링크가 없음")
    })
    public ResponseEntity<ApiResponse<BookPurchaseClickResponse>> recordPurchaseClick(
            Principal principal,
            @Parameter(description = "구매 링크를 조회할 도서 ID", example = "5")
            @PathVariable Long bookId
    ) {
        BookPurchaseClickResponse response = purchaseClickService.recordClick(
                Long.valueOf(principal.getName()), bookId);
        return ResponseEntity.ok(ApiResponse.success("이동 링크를 생성했습니다.", response));
    }
}
