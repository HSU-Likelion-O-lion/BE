package com.likelion.olion.domain.essay.repository;

import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;
import java.time.Instant;
import java.util.Optional;

public interface EssayRepository extends JpaRepository<Essay, Long> {
    Optional<Essay> findByEssayIdAndUserId(Long essayId, Long userId);

    List<Essay> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndCreatedAtAfterAndStatusIn(
            Long userId,
            Instant createdAt,
            Collection<EssayStatus> statuses
    );

    long countByUserIdAndLastRegeneratedAtAfter(Long userId, Instant since);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select essay from Essay essay where essay.essayId = :essayId")
    Optional<Essay> findByIdForUpdate(@Param("essayId") Long essayId);
}
