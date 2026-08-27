package com.example.myapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ユーザー認証情報を管理するエンティティ。
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ログインに使用するユーザー名。
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * ハッシュ化されたパスワード。
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * JPAが使用するデフォルトコンストラクタ。
     */
    protected User() {
    }

    /**
     * ユーザー登録用コンストラクタ。
     *
     * @param username ユーザー名
     * @param password ハッシュ化済みパスワード
     */
    public User(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

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
