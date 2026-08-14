package com.likelion.olion.domain.mate.repository;

import com.likelion.olion.domain.mate.entity.MatePin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatePinRepository extends JpaRepository<MatePin, Long> {
    List<MatePin> findByUserIdOrderByPinnedOrderAsc(Long userId);

    Optional<MatePin> findByUserIdAndUserBookUserBookId(Long userId, Long userBookId);

    boolean existsByUserIdAndUserBookUserBookId(Long userId, Long userBookId);

    long countByUserId(Long userId);
}
