package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    long countByUserIdAndCreatedAtAfter(Long userId, Instant createdAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select post from CommunityPost post where post.postId = :postId")
    Optional<CommunityPost> findByIdForUpdate(@Param("postId") Long postId);
}
