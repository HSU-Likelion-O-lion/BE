package com.likelion.olion.domain.reflection.repository;

import com.likelion.olion.domain.reflection.entity.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
    long countByUserId(Long userId);
}
