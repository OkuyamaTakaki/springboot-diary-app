package com.example.myapp;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * 日記データを管理する永続化エンティティ。
 * データベースの「diaries」テーブルとマッピングされます。
 */
@Entity
@Table(name = "diaries")
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ★修正ポイント：FetchType.LAZY から、確実に対象ユーザーのデータを一気に読み込む「EAGER」に変更しました。
    // これにより、画面（Thymeleaf）に日記一覧を渡す際のエラーが根本から消滅し、過去の日記が確実に表示されるようになります。
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * JPA仕様に伴うデフォルトコンストラクタ。
     */
    protected Diary() {
    }

    /**
     * 日記作成用のコンストラクタ。
     */
    public Diary(final String title, final String content) {
        this.title = title;
        this.content = content;
    }

    /* ==========================================================================
       JPA ライフサイクルコールバック（日付の自動設定・自動更新）
       ========================================================================== */
    @PrePersist
    protected void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* ==========================================================================
       ゲッター / セッター
       ========================================================================= */
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }
}