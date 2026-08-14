package com.likelion.olion.global.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiUsageServiceTest {
    @Mock
    private AiUsageRepository aiUsageRepository;

    @Test
    void 호출량이_제한보다_적으면_호출을_시작하고_기록한다() {
        Instant now = Instant.parse("2026-08-14T00:00:00Z");
        AiUsage usage = new AiUsage(7L, "essay-editing", AiUsageStatus.STARTED, now);
        ReflectionTestUtils.setField(usage, "usageId", 12L);
        given(aiUsageRepository.countByUserIdAndRequestedAtAfter(any(), any())).willReturn(0L);
        given(aiUsageRepository.saveAndFlush(any(AiUsage.class))).willReturn(usage);

        AiUsageService service = new AiUsageService(
                aiUsageRepository, Clock.fixed(now, ZoneOffset.UTC));

        assertThat(service.start(7L, "essay-editing")).contains(12L);
        verify(aiUsageRepository).saveAndFlush(any(AiUsage.class));
    }

    @Test
    void 호출량이_제한에_도달하면_AI_호출을_허용하지_않고_제한_기록을_남긴다() {
        Instant now = Instant.parse("2026-08-14T00:00:00Z");
        given(aiUsageRepository.countByUserIdAndRequestedAtAfter(any(), any()))
                .willReturn((long) AiUsageService.MAX_CALLS_PER_MINUTE);

        AiUsageService service = new AiUsageService(
                aiUsageRepository, Clock.fixed(now, ZoneOffset.UTC));

        assertThat(service.start(7L, "essay-editing")).isEmpty();
        verify(aiUsageRepository).save(any(AiUsage.class));
    }
}
