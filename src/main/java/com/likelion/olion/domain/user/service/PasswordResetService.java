package com.likelion.olion.domain.user.service;

import com.likelion.olion.domain.user.entity.PasswordResetToken;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.PasswordResetTokenRepository;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            mailService.sendPasswordResetLink(email, token);
        });
    }

    @Transactional
    public void confirmReset(String tokenValue, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "만료되었거나 이미 사용된 토큰입니다."));
        if (!resetToken.isValid()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "만료되었거나 이미 사용된 토큰입니다.");
        }
        User user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(newPassword));
        resetToken.use();
    }
}
