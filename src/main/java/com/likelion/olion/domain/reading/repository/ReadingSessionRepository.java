package com.likelion.olion.domain.reading.repository;

import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    boolean existsByUserIdAndStatus(Long userId, ReadingSessionStatus status);

    Optional<ReadingSession> findFirstByUserIdAndStatusOrderByStartedAtDesc(
            Long userId, ReadingSessionStatus status);

    Optional<ReadingSession> findBySessionIdAndUserId(Long sessionId, Long userId);

    List<ReadingSession> findByUserIdAndStatus(Long userId, ReadingSessionStatus status);

    List<ReadingSession> findByUserIdAndStatusAndStartedAtAfter(
            Long userId, ReadingSessionStatus status, Instant since);
}
