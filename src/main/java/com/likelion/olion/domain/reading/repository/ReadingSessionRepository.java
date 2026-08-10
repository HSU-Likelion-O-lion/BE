package com.likelion.olion.domain.reading.repository;

import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    boolean existsByUserIdAndStatus(Long userId, ReadingSessionStatus status);

    Optional<ReadingSession> findFirstByUserIdAndStatusOrderByStartedAtDesc(
            Long userId, ReadingSessionStatus status);
}
