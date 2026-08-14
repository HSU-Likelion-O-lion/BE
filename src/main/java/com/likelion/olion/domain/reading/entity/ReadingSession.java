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
import java.time.temporal.ChronoUnit;

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

    @Column(nullable = false)
    private Long focusedSeconds;

    private Instant activeStartedAt;

    private Instant pausedAt;

    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String aiQuestion;

    protected ReadingSession() {
    }

    public ReadingSession(Long userId, UserBook userBook, Integer targetMinutes) {
        this(userId, userBook, targetMinutes, Instant.now());
    }

    public ReadingSession(Long userId, UserBook userBook, Integer targetMinutes, Instant startedAt) {
        this.userId = userId;
        this.userBook = userBook;
        this.targetMinutes = targetMinutes;
        this.status = ReadingSessionStatus.IN_PROGRESS;
        this.startedAt = startedAt;
        this.focusedSeconds = 0L;
        this.activeStartedAt = startedAt;
    }

    public Long getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public UserBook getUserBook() { return userBook; }
    public Integer getTargetMinutes() { return targetMinutes; }
    public ReadingSessionStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Long getFocusedSeconds() { return focusedSeconds; }
    public Instant getActiveStartedAt() { return activeStartedAt; }
    public Instant getPausedAt() { return pausedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getAiQuestion() { return aiQuestion; }

    public void complete(String aiQuestion) {
        complete(aiQuestion, Instant.now());
        if (focusedSeconds == 0) {
            focusedSeconds = targetMinutes * 60L;
        }
    }

    public void complete(String aiQuestion, Instant completedAt) {
        settleActiveTime(completedAt);
        this.status = ReadingSessionStatus.COMPLETED;
        this.aiQuestion = aiQuestion;
        this.completedAt = completedAt;
    }

    public void abandon() {
        abandon(Instant.now());
    }

    public void abandon(Instant abandonedAt) {
        settleActiveTime(abandonedAt);
        this.status = ReadingSessionStatus.ABANDONED;
    }

    public void pause(Instant pausedAt) {
        if (status != ReadingSessionStatus.IN_PROGRESS) {
            return;
        }
        settleActiveTime(pausedAt);
        this.status = ReadingSessionStatus.PAUSED;
        this.pausedAt = pausedAt;
    }

    public void resume(Instant resumedAt) {
        if (status == ReadingSessionStatus.PAUSED) {
            this.status = ReadingSessionStatus.IN_PROGRESS;
            this.activeStartedAt = resumedAt;
            this.pausedAt = null;
        }
    }

    public long calculateFocusedSeconds(Instant now) {
        long total = focusedSeconds == null ? 0 : focusedSeconds;
        if (status == ReadingSessionStatus.IN_PROGRESS && activeStartedAt != null) {
            total += Math.max(0, activeStartedAt.until(now, ChronoUnit.SECONDS));
        }
        return total;
    }

    private void settleActiveTime(Instant settledAt) {
        if (activeStartedAt != null) {
            focusedSeconds = calculateFocusedSeconds(settledAt);
            activeStartedAt = null;
        }
    }
}
