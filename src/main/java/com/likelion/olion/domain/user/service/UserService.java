package com.likelion.olion.domain.user.service;

import com.likelion.olion.domain.user.dto.request.UpdatePlanRequest;
import com.likelion.olion.domain.user.dto.request.UpdateUserRequest;
import com.likelion.olion.domain.user.dto.response.ProfileImageResponse;
import com.likelion.olion.domain.user.dto.response.UpdatePlanResponse;
import com.likelion.olion.domain.user.dto.response.UpdateUserResponse;
import com.likelion.olion.domain.user.dto.response.UserMeResponse;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.PasswordResetTokenRepository;
import com.likelion.olion.domain.user.repository.RefreshTokenRepository;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = getUser(userId);
        return new UserMeResponse(user.getId(), user.getEmail(), user.getNickname(),
                fileStorageService.resolveProfileImageUrl(user.getProfileImageUrl()),
                user.getPlan(), user.getCreatedAt(), user.getUpdatedAt());
    }

    @Transactional
    public UpdatePlanResponse updatePlan(Long userId, UpdatePlanRequest request) {
        User user = getUser(userId);
        user.changePlan(request.plan());
        return new UpdatePlanResponse(user.getId(), user.getPlan());
    }

    @Transactional
    public UpdateUserResponse updateMe(Long userId, UpdateUserRequest request) {
        User user = getUser(userId);
        if (request.nickname() != null && !request.nickname().isBlank()) {
            if (userRepository.existsByNickname(request.nickname())) {
                throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 닉네임입니다.");
            }
            user.changeNickname(request.nickname());
        }
        return new UpdateUserResponse(user.getId(), user.getNickname(), user.getUpdatedAt());
    }

    @Transactional
    public void deleteMe(Long userId) {
        User user = getUser(userId);
        refreshTokenRepository.deleteAll(refreshTokenRepository.findAllByUserId(userId));
        passwordResetTokenRepository.deleteAll(passwordResetTokenRepository.findAllByUserId(userId));
        userRepository.delete(user);
    }

    @Transactional
    public ProfileImageResponse updateProfileImage(Long userId, MultipartFile image) {
        User user = getUser(userId);
        String url = fileStorageService.storeProfileImage(userId, image);
        user.changeProfileImageUrl(url);
        return new ProfileImageResponse(fileStorageService.resolveProfileImageUrl(url));
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
    }
}
