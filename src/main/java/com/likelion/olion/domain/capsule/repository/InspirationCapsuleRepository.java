package com.likelion.olion.domain.capsule.repository;

import com.likelion.olion.domain.capsule.entity.InspirationCapsule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InspirationCapsuleRepository extends JpaRepository<InspirationCapsule, Long> {
    Optional<InspirationCapsule> findByUserIdAndOpenedDate(Long userId, LocalDate openedDate);

    List<InspirationCapsule> findByUserIdOrderByOpenedDateDesc(Long userId);
}
