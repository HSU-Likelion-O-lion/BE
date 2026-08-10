package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    long countByUserIdAndCreatedAtAfter(Long userId, Instant createdAt);
}
