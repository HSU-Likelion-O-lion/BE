package com.likelion.olion.domain.book.entity;

import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "book_curations", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_book_curation_diagnosis_book",
                columnNames = {"diagnosis_id", "book_id"})
})
public class BookCuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long curationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EmotionDiagnosis diagnosis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String curationText;

    @Column(nullable = false)
    private Instant createdAt;

    protected BookCuration() {
    }

    public BookCuration(EmotionDiagnosis diagnosis, Book book, String curationText) {
        this.diagnosis = diagnosis;
        this.book = book;
        this.curationText = curationText;
        this.createdAt = Instant.now();
    }

    public Long getCurationId() { return curationId; }
    public EmotionDiagnosis getDiagnosis() { return diagnosis; }
    public Book getBook() { return book; }
    public String getCurationText() { return curationText; }
    public Instant getCreatedAt() { return createdAt; }
}
