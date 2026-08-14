package com.likelion.olion.domain.mate.controller;

import com.likelion.olion.domain.mate.dto.MatePinRequest;
import com.likelion.olion.domain.mate.dto.MatePinResponse;
import com.likelion.olion.domain.mate.dto.MatePinSaveResponse;
import com.likelion.olion.domain.mate.service.MatePinService;
import com.likelion.olion.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/mate/pins")
@Tag(name = "메이트 - 핀 도서", description = "메이트 화면에 표시할 책장 도서 핀 고정 API")
public class MatePinController {
    private final MatePinService matePinService;

    public MatePinController(MatePinService matePinService) {
        this.matePinService = matePinService;
    }

    @GetMapping
    @Operation(summary = "핀 고정 도서 조회", description = "로그인한 사용자가 메이트 화면에 핀 고정한 도서 목록을 순서대로 조회합니다.")
    public ResponseEntity<ApiResponse<MatePinResponse>> getPins(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "핀 고정 도서를 조회했습니다.", matePinService.getPins(getUserId(principal))));
    }

    @PostMapping
    @Operation(summary = "도서 핀 고정", description = "책장 도서 항목을 메이트 화면에 핀 고정합니다. 구독 등급에 따라 최대 개수(BASIC 3권, PLUS 5권, PRO 7권)가 다릅니다.")
    public ResponseEntity<ApiResponse<MatePinSaveResponse>> addPin(
            Principal principal,
            @Valid @RequestBody MatePinRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "핀 고정되었습니다.", matePinService.addPin(getUserId(principal), request)));
    }

    @DeleteMapping("/{userBookId}")
    @Operation(summary = "도서 핀 고정 해제", description = "메이트 화면에서 책장 도서 항목의 핀 고정을 해제합니다.")
    public ResponseEntity<Void> removePin(Principal principal,
            @Parameter(description = "책장 도서 항목 ID", example = "30") @PathVariable Long userBookId) {
        matePinService.removePin(getUserId(principal), userBookId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
