package com.likelion.olion.domain.user.controller;

import com.likelion.olion.domain.user.dto.request.LoginRequest;
import com.likelion.olion.domain.user.dto.request.LogoutRequest;
import com.likelion.olion.domain.user.dto.request.PasswordResetConfirmRequest;
import com.likelion.olion.domain.user.dto.request.PasswordResetRequest;
import com.likelion.olion.domain.user.dto.request.RefreshRequest;
import com.likelion.olion.domain.user.dto.request.SignUpRequest;
import com.likelion.olion.domain.user.dto.response.LoginResponse;
import com.likelion.olion.domain.user.dto.response.LogoutAllResponse;
import com.likelion.olion.domain.user.dto.response.RefreshResponse;
import com.likelion.olion.domain.user.dto.response.SignUpResponse;
import com.likelion.olion.domain.user.service.AuthService;
import com.likelion.olion.domain.user.service.PasswordResetService;
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
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "SUCCESS", HttpStatus.CREATED, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAll(Principal principal) {
        int count = authService.logoutAll(getUserId(principal));
        return ResponseEntity.ok(ApiResponse.success("모든 기기에서 로그아웃되었습니다.", new LogoutAllResponse(count)));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.success("재설정 링크가 발송되었습니다. (계정이 존재하는 경우)", null));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
