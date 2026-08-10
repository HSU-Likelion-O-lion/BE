package com.likelion.olion.domain.reading.entity;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reading_sessions")
public class ReadingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_book_id", nullable = false)
    private UserBook userBook;

    @Column(nullable = false)
    private Integer targetMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingSessionStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(columnDefinition = "TEXT")
    private String aiQuestion;

    protected ReadingSession() {
    }

    public ReadingSession(Long userId, UserBook userBook, Integer targetMinutes) {
        this.userId = userId;
        this.userBook = userBook;
        this.targetMinutes = targetMinutes;
        this.status = ReadingSessionStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public Long getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public UserBook getUserBook() { return userBook; }
    public Integer getTargetMinutes() { return targetMinutes; }
    public ReadingSessionStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public String getAiQuestion() { return aiQuestion; }

    public void complete(String aiQuestion) {
        this.status = ReadingSessionStatus.COMPLETED;
        this.aiQuestion = aiQuestion;
    }

    public void abandon() {
        this.status = ReadingSessionStatus.ABANDONED;
    }
}
