package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityShare;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityShareRepository extends JpaRepository<CommunityShare, Long> {
    Optional<CommunityShare> findByShareIdAndUserId(Long shareId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select share from CommunityShare share where share.shareId = :shareId")
    Optional<CommunityShare> findByIdForUpdate(@Param("shareId") Long shareId);
}
