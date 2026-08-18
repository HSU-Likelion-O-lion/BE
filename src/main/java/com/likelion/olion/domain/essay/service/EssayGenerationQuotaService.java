package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.entity.EssayStatus;
import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;

@Service
public class EssayGenerationQuotaService {
    private static final EnumSet<EssayStatus> COUNTED_STATUSES = EnumSet.of(
            EssayStatus.QUEUED, EssayStatus.PROCESSING, EssayStatus.COMPLETED);

    private final EssayRepository essayRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public EssayGenerationQuotaService(EssayRepository essayRepository, UserRepository userRepository) {
        this(essayRepository, userRepository, Clock.systemUTC());
    }

    EssayGenerationQuotaService(
            EssayRepository essayRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.essayRepository = essayRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public void validateAvailable(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        SubscriptionPlan plan = user.getPlan() == null ? SubscriptionPlan.BASIC : user.getPlan();
        int limit = plan.dailyEssayGenerationLimit();
        if (limit == Integer.MAX_VALUE) {
            return;
        }

        Instant startOfToday = LocalDate.now(clock).atStartOfDay(ZoneOffset.UTC).toInstant();
        long used = essayRepository.countByUserIdAndCreatedAtAfterAndStatusIn(
                userId, startOfToday, COUNTED_STATUSES);
        if (used >= limit) {
            throw new EssayGenerationQuotaExceededException(limit);
        }
    }

    public void validateRegenerationAvailable(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Instant startOfToday = LocalDate.now(clock).atStartOfDay(ZoneOffset.UTC).toInstant();
        if (essayRepository.countByUserIdAndLastRegeneratedAtAfter(userId, startOfToday) >= 1) {
            throw new EssayRegenerationQuotaExceededException();
        }
    }

    public static class EssayGenerationQuotaExceededException extends RuntimeException {
        public EssayGenerationQuotaExceededException(int limit) {
            super("오늘의 에세이 생성 가능 횟수를 초과했습니다. (최대 " + limit + "회)");
        }
    }

    public static class EssayRegenerationQuotaExceededException extends RuntimeException {
        public EssayRegenerationQuotaExceededException() {
            super("오늘의 에세이 재생성 가능 횟수를 초과했습니다. (최대 1회)");
        }
    }
}
