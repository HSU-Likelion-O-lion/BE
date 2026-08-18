package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class EssayGenerationQuotaServiceTest {
    private final EssayRepository essayRepository = mock(EssayRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final EssayGenerationQuotaService service = new EssayGenerationQuotaService(
            essayRepository,
            userRepository,
            Clock.fixed(Instant.parse("2026-08-18T09:00:00Z"), ZoneOffset.UTC));

    @Test
    void allowsBasicPlanWhenNoEssayWasCreatedToday() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@a.com", "pw", "reader")));
        given(essayRepository.countByUserIdAndCreatedAtAfterAndStatusIn(
                eq(1L), any(Instant.class), any())).willReturn(0L);

        assertThatCode(() -> service.validateAvailable(1L)).doesNotThrowAnyException();
    }

    @Test
    void rejectsBasicPlanAfterOneActiveOrCompletedEssayToday() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@a.com", "pw", "reader")));
        given(essayRepository.countByUserIdAndCreatedAtAfterAndStatusIn(
                eq(1L), any(Instant.class), any())).willReturn(1L);

        assertThatThrownBy(() -> service.validateAvailable(1L))
                .isInstanceOf(EssayGenerationQuotaService.EssayGenerationQuotaExceededException.class);
    }

    @Test
    void allowsOneRegenerationPerDay() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@a.com", "pw", "reader")));
        given(essayRepository.countByUserIdAndLastRegeneratedAtAfter(eq(1L), any(Instant.class)))
                .willReturn(0L);

        assertThatCode(() -> service.validateRegenerationAvailable(1L)).doesNotThrowAnyException();
    }

    @Test
    void rejectsSecondRegenerationOnTheSameDay() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@a.com", "pw", "reader")));
        given(essayRepository.countByUserIdAndLastRegeneratedAtAfter(eq(1L), any(Instant.class)))
                .willReturn(1L);

        assertThatThrownBy(() -> service.validateRegenerationAvailable(1L))
                .isInstanceOf(EssayGenerationQuotaService.EssayRegenerationQuotaExceededException.class);
    }
}
