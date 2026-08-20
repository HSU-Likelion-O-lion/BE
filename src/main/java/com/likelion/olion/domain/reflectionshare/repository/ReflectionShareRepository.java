package com.likelion.olion.domain.reflectionshare.repository;

import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReflectionShareRepository extends JpaRepository<ReflectionShare, Long> {
    Optional<ReflectionShare> findByShareIdAndUserId(Long shareId, Long userId);

    List<ReflectionShare> findTop50ByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            Collection<ReflectionShareStatus> statuses,
            Instant updatedAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select share from ReflectionShare share where share.shareId = :shareId")
    Optional<ReflectionShare> findByIdForUpdate(@Param("shareId") Long shareId);
}
