package com.likelion.olion.domain.community.entity;

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
@Table(name = "community_post_hearts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_community_heart_post_user", columnNames = {"post_id", "user_id"})
})
public class CommunityPostHeart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long heartId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CommunityPost post;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    protected CommunityPostHeart() {
    }

    public CommunityPostHeart(CommunityPost post, Long userId) {
        this.post = post;
        this.userId = userId;
        this.createdAt = Instant.now();
    }

    public Long getHeartId() { return heartId; }
    public CommunityPost getPost() { return post; }
    public Long getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
}
