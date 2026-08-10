package com.likelion.olion.domain.user.service;

import com.likelion.olion.domain.user.dto.request.LoginRequest;
import com.likelion.olion.domain.user.dto.request.SignUpRequest;
import com.likelion.olion.domain.user.dto.response.LoginResponse;
import com.likelion.olion.domain.user.dto.response.RefreshResponse;
import com.likelion.olion.domain.user.dto.response.SignUpResponse;
import com.likelion.olion.domain.user.entity.RefreshToken;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.RefreshTokenRepository;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import com.likelion.olion.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-validity-ms}")
    private long refreshTokenValidityMs;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        emailVerificationService.assertVerified(request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        userRepository.save(user);
        return new SignUpResponse(user.getId(), user.getEmail(), user.getNickname(), user.getCreatedAt());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "잘못된 이메일 또는 비밀번호입니다.");
        }
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        RefreshToken refreshToken = issueRefreshToken(user);
        return new LoginResponse(user.getId(), user.getNickname(), accessToken, refreshToken.getToken());
    }

    @Transactional
    public RefreshResponse reissue(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "만료되었거나 유효하지 않은 토큰입니다."));
        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이미 폐기된 토큰입니다. 재로그인이 필요합니다.");
        }
        if (refreshToken.isExpired()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "만료되었거나 유효하지 않은 토큰입니다.");
        }
        refreshToken.revoke();
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
        RefreshToken newRefreshToken = issueRefreshToken(user);
        return new RefreshResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "토큰을 찾을 수 없습니다."));
        refreshToken.revoke();
    }

    @Transactional
    public int logoutAll(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
        tokens.forEach(RefreshToken::revoke);
        return tokens.size();
    }

    private RefreshToken issueRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenValidityMs)))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
