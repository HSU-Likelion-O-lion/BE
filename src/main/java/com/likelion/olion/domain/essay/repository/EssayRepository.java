package com.likelion.olion.domain.essay.repository;

import com.likelion.olion.domain.essay.entity.Essay;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EssayRepository extends JpaRepository<Essay, Long> {
    Optional<Essay> findByEssayIdAndUserId(Long essayId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select essay from Essay essay where essay.essayId = :essayId")
    Optional<Essay> findByIdForUpdate(@Param("essayId") Long essayId);
}
