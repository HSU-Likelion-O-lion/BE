package com.likelion.olion.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Readmate] 이메일 인증번호");
        message.setText("인증번호는 [" + code + "] 입니다. 5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    public void sendPasswordResetLink(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Readmate] 비밀번호 재설정");
        message.setText("아래 토큰으로 비밀번호를 재설정해주세요.\n" + token);
        mailSender.send(message);
    }
}
