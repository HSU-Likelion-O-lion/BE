package com.likelion.olion.domain.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "community_share_themes")
public class CommunityShareTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long themeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String previewUrl;

    protected CommunityShareTheme() {
    }

    public CommunityShareTheme(String name, String previewUrl) {
        this.name = name;
        this.previewUrl = previewUrl;
    }

    public Long getThemeId() { return themeId; }
    public String getName() { return name; }
    public String getPreviewUrl() { return previewUrl; }
}
