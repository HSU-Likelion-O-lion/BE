package com.likelion.olion.domain.user.service;

import com.likelion.olion.domain.user.entity.EmailVerification;
import com.likelion.olion.domain.user.repository.EmailVerificationRepository;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final long CODE_VALIDITY_MINUTES = 5;
    private static final long VERIFIED_VALIDITY_MINUTES = 30;

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        String code = generateCode();
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES))
                .build();
        emailVerificationRepository.save(verification);
        mailService.sendVerificationCode(email, code);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "인증번호가 올바르지 않습니다."));
        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "인증번호가 만료되었습니다.");
        }
        if (!verification.matches(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "인증번호가 올바르지 않습니다.");
        }
        verification.verify();
    }

    public void assertVerified(String email) {
        EmailVerification verification = emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "이메일 인증이 필요합니다."));
        boolean stillValid = verification.isVerified()
                && verification.getCreatedAt().plusMinutes(VERIFIED_VALIDITY_MINUTES).isAfter(LocalDateTime.now());
        if (!stillValid) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일 인증이 필요합니다.");
        }
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
