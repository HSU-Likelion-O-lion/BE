package com.likelion.olion.domain.book.repository;

import com.likelion.olion.domain.book.entity.BookCuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCurationRepository extends JpaRepository<BookCuration, Long> {
    Optional<BookCuration> findByDiagnosisDiagnosisIdAndBookBookId(
            Long diagnosisId, Long bookId);
}
