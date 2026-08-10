package com.likelion.olion.domain.reading.repository;

import com.likelion.olion.domain.reading.entity.ReadingInterruption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingInterruptionRepository extends JpaRepository<ReadingInterruption, Long> {
    List<ReadingInterruption> findBySessionUserId(Long userId);
}
