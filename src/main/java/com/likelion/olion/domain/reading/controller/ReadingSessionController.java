package com.likelion.olion.domain.reading.controller;

import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.service.ReadingSessionService;
import com.likelion.olion.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/reading-sessions")
public class ReadingSessionController {
    private final ReadingSessionService readingSessionService;

    public ReadingSessionController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReadingSessionStartResponse>> start(
            Principal principal,
            @Valid @RequestBody ReadingSessionStartRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "몰입 세션이 시작되었습니다.",
                readingSessionService.start(Long.valueOf(principal.getName()), request)));
    }
}
