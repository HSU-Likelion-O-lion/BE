package com.likelion.olion.domain.community.entity;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "community_shares")
public class CommunityShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private CommunityShareTheme theme;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityShareStatus status;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CommunityShare() {
    }

    public CommunityShare(CommunityPost post, CommunityShareTheme theme, Long userId) {
        this.post = post;
        this.theme = theme;
        this.userId = userId;
        this.status = CommunityShareStatus.QUEUED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void startProcessing() {
        this.status = CommunityShareStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void complete(String imageUrl) {
        this.status = CommunityShareStatus.COMPLETED;
        this.imageUrl = imageUrl;
        this.updatedAt = Instant.now();
    }

    public void requeue() {
        this.status = CommunityShareStatus.QUEUED;
        this.imageUrl = null;
        this.updatedAt = Instant.now();
    }

    public Long getShareId() { return shareId; }
    public CommunityPost getPost() { return post; }
    public CommunityShareTheme getTheme() { return theme; }
    public Long getUserId() { return userId; }
    public CommunityShareStatus getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
