package com.likelion.olion.domain.reflection.repository;

import com.likelion.olion.domain.reflection.entity.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
    long countByUserId(Long userId);

    List<Reflection> findByUserIdOrderByCreatedAtDesc(Long userId);
}
