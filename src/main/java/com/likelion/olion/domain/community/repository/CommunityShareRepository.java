package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityShareRepository extends JpaRepository<CommunityShare, Long> {
    Optional<CommunityShare> findByShareIdAndUserId(Long shareId, Long userId);
}
