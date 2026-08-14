package com.likelion.olion.domain.essay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "essay_chapters")
public class EssayChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chapterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "essay_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Essay essay;

    @Column(nullable = false)
    private Integer chapterNo;

    @Column(nullable = false)
    private String title;

    protected EssayChapter() {
    }

    public EssayChapter(Essay essay, Integer chapterNo, String title) {
        this.essay = essay;
        this.chapterNo = chapterNo;
        this.title = title;
    }

    public Long getChapterId() { return chapterId; }
    public Essay getEssay() { return essay; }
    public Integer getChapterNo() { return chapterNo; }
    public String getTitle() { return title; }
}
