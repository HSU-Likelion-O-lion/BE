package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityPostHeart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostHeartRepository extends JpaRepository<CommunityPostHeart, Long> {
    boolean existsByPostPostIdAndUserId(Long postId, Long userId);

    Optional<CommunityPostHeart> findByPostPostIdAndUserId(Long postId, Long userId);

    long countByPostPostId(Long postId);
}
