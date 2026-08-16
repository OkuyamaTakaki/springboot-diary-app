package com.example.myapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ユーザー認証情報を管理する永続化エンティティ。
 * データベースの「users」テーブルとマッピングされます。
 */
@Entity
@Table(name = "users") // テーブル名を明示的に指定
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 実務必須：ユーザー名はログインIDとなるため、NULL不許可かつ「一意（重複禁止）」のユニーク制約をDBレベルで設定
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    // 実務必須：ハッシュ化されたパスワードが安全に格納されるよう、カラムの文字長に十分な余裕（BCrypt等は約60文字）を持たせる
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * JPAの仕様に伴うデフォルトコンストラクタ。
     * カプセル化を強化し、外部から不用意に空のオブジェクトが作られるのを防ぐためアクセス制限を protected に設定。
     */
    protected User() {
    }

    /**
     * 新規ユーザー登録用のコンストラクタ。
     * 
     * @param username 登録するユーザー名（ログインID）
     * @param password 暗号化済みのパスワード
     */
    public User(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /* ==========================================================================
       ゲッター / セッター
       ========================================================================== */

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}