package com.likelion.olion.domain.community.repository;

import com.likelion.olion.domain.community.entity.CommunityShareTheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityShareThemeRepository extends JpaRepository<CommunityShareTheme, Long> {
    List<CommunityShareTheme> findAllByOrderByThemeIdAsc();
}
