package com.likelion.olion.domain.user.service;

import com.likelion.olion.domain.user.dto.request.UpdatePlanRequest;
import com.likelion.olion.domain.user.dto.response.UpdatePlanResponse;
import com.likelion.olion.domain.user.dto.response.UserMeResponse;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.PasswordResetTokenRepository;
import com.likelion.olion.domain.user.repository.RefreshTokenRepository;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    @Test
    void returnsDefaultBasicPlanForNewUser() {
        User user = User.builder().email("test@example.com").password("encoded").nickname("닉네임").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserMeResponse response = userService.getMe(1L);

        assertThat(response.plan()).isEqualTo(SubscriptionPlan.BASIC);
    }

    @Test
    void changesPlanToRequestedValue() {
        User user = User.builder().email("test@example.com").password("encoded").nickname("닉네임").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UpdatePlanResponse response = userService.updatePlan(1L, new UpdatePlanRequest(SubscriptionPlan.PRO));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.plan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(user.getPlan()).isEqualTo(SubscriptionPlan.PRO);
    }

    @Test
    void rejectsPlanChangeWhenUserNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePlan(1L, new UpdatePlanRequest(SubscriptionPlan.PLUS)))
                .isInstanceOf(BusinessException.class);
    }
}
