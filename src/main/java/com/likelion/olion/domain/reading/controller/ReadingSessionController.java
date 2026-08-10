package com.likelion.olion.domain.reading.controller;

import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.dto.ActiveReadingSessionResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionResumeResponse;
import com.likelion.olion.domain.reading.service.ReadingSessionService;
import com.likelion.olion.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ActiveReadingSessionResponse>> getActive(Principal principal) {
        ActiveReadingSessionResponse response = readingSessionService
                .getActive(Long.valueOf(principal.getName()));
        String message = response.session() == null
                ? "진행 중인 세션이 없습니다."
                : "진행 중인 세션이 있습니다.";
        String code = response.session() == null ? "SUCCESS_NONE" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(code, HttpStatus.OK, message, response));
    }

    @PostMapping("/{sessionId}/heartbeat")
    public ResponseEntity<ApiResponse<ReadingSessionHeartbeatResponse>> heartbeat(
            Principal principal,
            @PathVariable Long sessionId,
            @Valid @RequestBody ReadingSessionHeartbeatRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "세션이 갱신되었습니다.",
                readingSessionService.heartbeat(
                        Long.valueOf(principal.getName()), sessionId, request)));
    }

    @PatchMapping("/{sessionId}/resume")
    public ResponseEntity<ApiResponse<ReadingSessionResumeResponse>> resume(
            Principal principal,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "세션이 재개되었습니다.",
                readingSessionService.resume(Long.valueOf(principal.getName()), sessionId)));
    }
}
