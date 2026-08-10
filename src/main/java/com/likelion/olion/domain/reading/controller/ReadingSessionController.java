package com.likelion.olion.domain.reading.controller;

import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.dto.ActiveReadingSessionResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionResumeResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionCompleteResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionAbandonResponse;
import com.likelion.olion.domain.reading.dto.ReadingInterruptionRequest;
import com.likelion.olion.domain.reading.dto.ReadingInterruptionResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reading-sessions")
@Tag(name = "독서 세션", description = "독서 몰입 세션의 시작·조회·동기화·재개·완료 API")
public class ReadingSessionController {
    private final ReadingSessionService readingSessionService;

    public ReadingSessionController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    @PostMapping
    @Operation(summary = "독서 세션 시작", description = "책장 도서와 목표 시간을 지정해 독서 몰입 세션을 시작합니다. 목표 시간은 15, 30, 60분만 허용됩니다.")
    public ResponseEntity<ApiResponse<ReadingSessionStartResponse>> start(
            Principal principal,
            @Valid @RequestBody ReadingSessionStartRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "몰입 세션이 시작되었습니다.",
                readingSessionService.start(Long.valueOf(principal.getName()), request)));
    }

    @GetMapping("/active")
    @Operation(summary = "진행 중 세션 조회", description = "현재 진행 중인 세션과 서버 기준 남은 시간을 조회합니다. 진행 중인 세션이 없으면 session이 null입니다.")
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
    @Operation(summary = "세션 heartbeat", description = "클라이언트의 경과 시간을 서버 시간과 비교하고 서버 기준 남은 시간을 반환합니다.")
    public ResponseEntity<ApiResponse<ReadingSessionHeartbeatResponse>> heartbeat(
            Principal principal,
            @Parameter(description = "독서 세션 ID", example = "100")
            @PathVariable Long sessionId,
            @Valid @RequestBody ReadingSessionHeartbeatRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "세션이 갱신되었습니다.",
                readingSessionService.heartbeat(
                        Long.valueOf(principal.getName()), sessionId, request)));
    }

    @PatchMapping("/{sessionId}/resume")
    @Operation(summary = "독서 세션 재개", description = "이탈 후 기존 독서 세션을 다시 이어 읽습니다. 종료된 세션은 재개할 수 없습니다.")
    public ResponseEntity<ApiResponse<ReadingSessionResumeResponse>> resume(
            Principal principal,
            @Parameter(description = "독서 세션 ID", example = "100")
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "세션이 재개되었습니다.",
                readingSessionService.resume(Long.valueOf(principal.getName()), sessionId)));
    }

    @PatchMapping("/{sessionId}/complete")
    @Operation(summary = "독서 세션 완료", description = "독서 목표 달성 세션을 완료 처리하고 독서 후 기록을 위한 질문을 반환합니다.")
    public ResponseEntity<ApiResponse<ReadingSessionCompleteResponse>> complete(
            Principal principal,
            @Parameter(description = "독서 세션 ID", example = "100")
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "목표를 달성했습니다.",
                readingSessionService.complete(Long.valueOf(principal.getName()), sessionId)));
    }

    @PatchMapping("/{sessionId}/abandon")
    @Operation(summary = "독서 세션 강제 종료", description = "목표 시간 도달 전에 사용자가 독서 세션을 직접 종료합니다.")
    public ResponseEntity<ApiResponse<ReadingSessionAbandonResponse>> abandon(
            Principal principal,
            @Parameter(description = "독서 세션 ID", example = "100")
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "세션이 종료되었습니다.",
                readingSessionService.abandon(Long.valueOf(principal.getName()), sessionId)));
    }

    @PostMapping("/{sessionId}/interruptions")
    @Operation(summary = "세션 이탈 사유 기록", description = "독서 세션 중 발생한 이탈 사유와 발생 시각을 기록합니다. OTHER 선택 시 customText가 필요합니다.")
    public ResponseEntity<ApiResponse<ReadingInterruptionResponse>> recordInterruption(
            Principal principal,
            @Parameter(description = "독서 세션 ID", example = "100")
            @PathVariable Long sessionId,
            @Valid @RequestBody ReadingInterruptionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "이탈 사유가 기록되었습니다.",
                readingSessionService.recordInterruption(
                        Long.valueOf(principal.getName()), sessionId, request)));
    }

    @DeleteMapping("/{sessionId}/recovery")
    @Operation(summary = "복구 세션 삭제", description = "이전 세션을 복구하지 않고 삭제합니다. 삭제 후 새 독서 세션을 시작할 수 있습니다.")
    public ResponseEntity<Void> deleteRecoverySession(
            Principal principal,
            @Parameter(description = "삭제할 독서 세션 ID", example = "100")
            @PathVariable Long sessionId
    ) {
        readingSessionService.deleteRecoverySession(Long.valueOf(principal.getName()), sessionId);
        return ResponseEntity.noContent().build();
    }
}
