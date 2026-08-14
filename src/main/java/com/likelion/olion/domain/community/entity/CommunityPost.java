package com.likelion.olion.domain.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "community_posts")
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String anonymousNickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Long reflectionId;

    @Column(nullable = false)
    private boolean blinded;

    @Column(nullable = false)
    private Instant createdAt;

    protected CommunityPost() {
    }

    public CommunityPost(Long roomId, Long userId, String anonymousNickname, String content) {
        this(roomId, userId, anonymousNickname, content, null);
    }

    public CommunityPost(
            Long roomId,
            Long userId,
            String anonymousNickname,
            String content,
            Long reflectionId
    ) {
        this.roomId = roomId;
        this.userId = userId;
        this.anonymousNickname = anonymousNickname;
        this.content = content;
        this.reflectionId = reflectionId;
        this.blinded = false;
        this.createdAt = Instant.now();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void blind() {
        this.blinded = true;
    }

    public Long getPostId() { return postId; }
    public Long getRoomId() { return roomId; }
    public Long getUserId() { return userId; }
    public String getAnonymousNickname() { return anonymousNickname; }
    public String getContent() { return content; }
    public Long getReflectionId() { return reflectionId; }
    public boolean isBlinded() { return blinded; }
    public Instant getCreatedAt() { return createdAt; }
}
