package com.likelion.olion.domain.reflection.repository;

import com.likelion.olion.domain.reflection.entity.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
    long countByUserId(Long userId);

    List<Reflection> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Reflection> findByReflectionIdAndUserId(Long reflectionId, Long userId);
}
