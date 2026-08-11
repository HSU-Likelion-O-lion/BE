package com.likelion.olion.domain.reflection.entity;

import com.likelion.olion.domain.reading.entity.ReadingSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reflections")
public class Reflection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reflectionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingSession session;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Reflection() {
    }

    public Reflection(Long userId, ReadingSession session, String content) {
        this.userId = userId;
        this.session = session;
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getReflectionId() { return reflectionId; }
    public Long getUserId() { return userId; }
    public ReadingSession getSession() { return session; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void edit(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }
}
