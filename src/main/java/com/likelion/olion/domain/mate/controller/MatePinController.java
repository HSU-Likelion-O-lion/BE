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

@RestController
@RequestMapping("/api/mate/pins")
public class MatePinController {
    private final MatePinService matePinService;

    public MatePinController(MatePinService matePinService) {
        this.matePinService = matePinService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MatePinResponse>> getPins(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "핀 고정 도서를 조회했습니다.", matePinService.getPins(getUserId(principal))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MatePinSaveResponse>> addPin(
            Principal principal,
            @Valid @RequestBody MatePinRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "핀 고정되었습니다.", matePinService.addPin(getUserId(principal), request)));
    }

    @DeleteMapping("/{userBookId}")
    public ResponseEntity<Void> removePin(Principal principal, @PathVariable Long userBookId) {
        matePinService.removePin(getUserId(principal), userBookId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
