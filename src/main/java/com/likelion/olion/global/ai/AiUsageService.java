package com.likelion.olion.global.ai;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AiUsageService {
    static final int MAX_CALLS_PER_MINUTE = 10;

    private final AiUsageRepository aiUsageRepository;
    private final Clock clock;

    @Autowired
    public AiUsageService(AiUsageRepository aiUsageRepository) {
        this(aiUsageRepository, Clock.systemUTC());
    }

    AiUsageService(AiUsageRepository aiUsageRepository, Clock clock) {
        this.aiUsageRepository = aiUsageRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> start(Long userId, String feature) {
        Instant now = Instant.now(clock);
        Instant windowStart = now.minus(1, ChronoUnit.MINUTES);
        long count = userId == null
                ? aiUsageRepository.countByUserIdIsNullAndRequestedAtAfter(windowStart)
                : aiUsageRepository.countByUserIdAndRequestedAtAfter(userId, windowStart);
        if (count >= MAX_CALLS_PER_MINUTE) {
            aiUsageRepository.save(new AiUsage(
                    userId, normalizeFeature(feature), AiUsageStatus.RATE_LIMITED, now));
            return Optional.empty();
        }

        AiUsage usage = aiUsageRepository.saveAndFlush(new AiUsage(
                userId, normalizeFeature(feature), AiUsageStatus.STARTED, now));
        return Optional.of(usage.getUsageId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long usageId, AiUsageStatus status, Instant completedAt, long durationMillis) {
        aiUsageRepository.findById(usageId)
                .ifPresent(usage -> usage.complete(status, completedAt, durationMillis));
    }

    private String normalizeFeature(String feature) {
        if (feature == null || feature.isBlank()) {
            return "unknown";
        }
        return feature.length() <= 50 ? feature : feature.substring(0, 50);
    }
}
