package com.likelion.olion.domain.user.controller;

import com.likelion.olion.domain.user.dto.request.KakaoLoginRequest;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증", description = "회원가입·로그인·토큰·비밀번호 재설정 API")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입합니다.")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "SUCCESS", HttpStatus.CREATED, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 액세스/리프레시 토큰을 발급받습니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "프론트에서 카카오 SDK로 발급받은 액세스 토큰으로 로그인합니다. "
            + "처음 로그인하는 사용자는 자동으로 회원가입 처리됩니다. 닉네임/프로필은 이후 온보딩에서 별도로 설정합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        LoginResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(ApiResponse.success("카카오 로그인에 성공했습니다.", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 새로운 액세스/리프레시 토큰을 발급받습니다.")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "전달한 리프레시 토큰을 폐기하여 해당 기기에서 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃", description = "로그인한 사용자의 모든 기기에서 발급된 리프레시 토큰을 전부 폐기합니다.")
    public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAll(Principal principal) {
        int count = authService.logoutAll(getUserId(principal));
        return ResponseEntity.ok(ApiResponse.success("모든 기기에서 로그아웃되었습니다.", new LogoutAllResponse(count)));
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "비밀번호 재설정 요청", description = "가입된 이메일로 비밀번호 재설정 링크(토큰)를 발송합니다.")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.success("재설정 링크가 발송되었습니다. (계정이 존재하는 경우)", null));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정", description = "발급받은 토큰과 새 비밀번호로 비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
