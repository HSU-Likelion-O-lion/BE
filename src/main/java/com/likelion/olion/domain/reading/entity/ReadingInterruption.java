package com.likelion.olion.domain.reading.entity;

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
@Table(name = "reading_interruptions")
public class ReadingInterruption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interruptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingInterruptionReason reason;

    @Column(length = 500)
    private String customText;

    @Column(nullable = false)
    private Instant occurredAt;

    protected ReadingInterruption() {
    }

    public ReadingInterruption(ReadingSession session, ReadingInterruptionReason reason,
                               String customText, Instant occurredAt) {
        this.session = session;
        this.reason = reason;
        this.customText = customText;
        this.occurredAt = occurredAt;
    }

    public Long getInterruptionId() { return interruptionId; }
    public ReadingSession getSession() { return session; }
    public ReadingInterruptionReason getReason() { return reason; }
    public String getCustomText() { return customText; }
    public Instant getOccurredAt() { return occurredAt; }
}
