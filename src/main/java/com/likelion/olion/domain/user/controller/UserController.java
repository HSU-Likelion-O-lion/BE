package com.likelion.olion.domain.user.controller;

import com.likelion.olion.domain.user.dto.request.UpdatePlanRequest;
import com.likelion.olion.domain.user.dto.request.UpdateUserRequest;
import com.likelion.olion.domain.user.dto.response.AvailableResponse;
import com.likelion.olion.domain.user.dto.response.ProfileImageResponse;
import com.likelion.olion.domain.user.dto.response.UpdatePlanResponse;
import com.likelion.olion.domain.user.dto.response.UpdateUserResponse;
import com.likelion.olion.domain.user.dto.response.UserMeResponse;
import com.likelion.olion.domain.user.service.UserService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@Tag(name = "회원", description = "회원 정보 조회·수정·탈퇴 API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자 본인의 회원 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMe(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("회원정보 조회에 성공했습니다.", userService.getMe(getUserId(principal))));
    }

    @PatchMapping("/api/users/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자 본인의 닉네임 등 회원 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateMe(Principal principal,
                                                                      @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("회원 정보가 수정되었습니다.", userService.updateMe(getUserId(principal), request)));
    }

    @PatchMapping("/api/users/me/plan")
    @Operation(summary = "구독 등급 변경", description = "프론트에서 결제(시늉)를 완료한 뒤, 로그인한 사용자 본인의 구독 등급을 변경합니다.")
    public ResponseEntity<ApiResponse<UpdatePlanResponse>> updatePlan(Principal principal,
                                                                        @Valid @RequestBody UpdatePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("구독 등급이 변경되었습니다.", userService.updatePlan(getUserId(principal), request)));
    }

    @DeleteMapping("/api/users/me")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자 본인의 계정을 탈퇴 처리합니다.")
    public ResponseEntity<Void> deleteMe(Principal principal) {
        userService.deleteMe(getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users/check-email")
    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 입력한 이메일이 사용 가능한지 확인합니다.")
    public ResponseEntity<ApiResponse<AvailableResponse>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success("이메일 중복을 확인했습니다.", new AvailableResponse(userService.isEmailAvailable(email))));
    }

    @GetMapping("/api/users/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 입력한 닉네임이 사용 가능한지 확인합니다.")
    public ResponseEntity<ApiResponse<AvailableResponse>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(ApiResponse.success("닉네임 중복을 확인했습니다.", new AvailableResponse(userService.isNicknameAvailable(nickname))));
    }

    @PostMapping("/api/users/me/profile-image")
    @Operation(summary = "프로필 이미지 변경", description = "로그인한 사용자 본인의 프로필 이미지를 업로드하여 변경합니다.")
    public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(Principal principal,
                                                                                  @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 변경되었습니다.", userService.updateProfileImage(getUserId(principal), image)));
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
