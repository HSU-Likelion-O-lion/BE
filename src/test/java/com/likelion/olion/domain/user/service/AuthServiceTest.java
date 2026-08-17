package com.likelion.olion.domain.user.service;

import com.likelion.olion.domain.user.client.KakaoUserInfoClient;
import com.likelion.olion.domain.user.dto.request.KakaoLoginRequest;
import com.likelion.olion.domain.user.dto.request.LoginRequest;
import com.likelion.olion.domain.user.dto.response.LoginResponse;
import com.likelion.olion.domain.user.entity.AuthProvider;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.RefreshTokenRepository;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private KakaoUserInfoClient kakaoUserInfoClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void rejectsLoginWhenUserHasNoPassword() {
        User kakaoUser = User.ofKakao("kakao_1@olion.internal", "카카오사용자1", "1");
        ReflectionTestUtils.setField(kakaoUser, "id", 1L);
        given(userRepository.findByEmail("kakao_1@olion.internal")).willReturn(Optional.of(kakaoUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("kakao_1@olion.internal", "aaaaaaaa")))
                .isInstanceOf(BusinessException.class);

        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    void logsInExistingKakaoUserWithoutCreatingDuplicate() {
        User existing = User.ofKakao("kakao_123@olion.internal", "카카오사용자123", "123");
        ReflectionTestUtils.setField(existing, "id", 5L);
        given(kakaoUserInfoClient.getUserInfo("access-token"))
                .willReturn(new KakaoUserInfoClient.KakaoUserInfo("123"));
        given(userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "123"))
                .willReturn(Optional.of(existing));
        given(jwtTokenProvider.generateAccessToken(5L)).willReturn("access-jwt");
        given(refreshTokenRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = authService.kakaoLogin(new KakaoLoginRequest("access-token"));

        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.nickname()).isEqualTo("카카오사용자123");
        assertThat(response.accessToken()).isEqualTo("access-jwt");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registersNewUserOnFirstKakaoLogin() {
        given(kakaoUserInfoClient.getUserInfo("access-token"))
                .willReturn(new KakaoUserInfoClient.KakaoUserInfo("999"));
        given(userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "999"))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });
        given(jwtTokenProvider.generateAccessToken(10L)).willReturn("access-jwt");
        given(refreshTokenRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = authService.kakaoLogin(new KakaoLoginRequest("access-token"));

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.nickname()).isEqualTo("카카오사용자999");
        verify(userRepository).save(any(User.class));
    }
}
