package com.likelion.olion.domain.reading.repository;

import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    boolean existsByUserIdAndStatus(Long userId, ReadingSessionStatus status);
}
