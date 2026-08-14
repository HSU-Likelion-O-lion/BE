package com.likelion.olion.domain.capsule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "inspiration_capsules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_capsule_user_date", columnNames = {"user_id", "opened_date"})
})
public class InspirationCapsule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long capsuleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String quoteText;

    private String bookTitle;

    @Column(nullable = false)
    private LocalDate openedDate;

    @Column(nullable = false)
    private Instant createdAt;

    protected InspirationCapsule() {
    }

    public InspirationCapsule(Long userId, String quoteText, String bookTitle, LocalDate openedDate) {
        this.userId = userId;
        this.quoteText = quoteText;
        this.bookTitle = bookTitle;
        this.openedDate = openedDate;
        this.createdAt = Instant.now();
    }

    public Long getCapsuleId() { return capsuleId; }
    public Long getUserId() { return userId; }
    public String getQuoteText() { return quoteText; }
    public String getBookTitle() { return bookTitle; }
    public LocalDate getOpenedDate() { return openedDate; }
    public Instant getCreatedAt() { return createdAt; }
}
