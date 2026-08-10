package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityPostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostReportRepository extends JpaRepository<CommunityPostReport, Long> {
    boolean existsByPostPostIdAndUserId(Long postId, Long userId);

    long countByPostPostId(Long postId);
}
