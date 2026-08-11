package com.likelion.olion.domain.essay.repository;

import com.likelion.olion.domain.essay.entity.EssayChapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EssayChapterRepository extends JpaRepository<EssayChapter, Long> {
    List<EssayChapter> findByEssay_EssayIdOrderByChapterNo(Long essayId);
}
