package com.likelion.olion.global.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {
    long countByUserIdAndRequestedAtAfter(Long userId, Instant requestedAt);

    long countByUserIdIsNullAndRequestedAtAfter(Instant requestedAt);
}
