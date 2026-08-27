package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * H2 Console専用の管理者認証を担当するサービス。
 */
@Service
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class H2UserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(H2UserDetailsService.class);

    private final PasswordEncoder passwordEncoder;

    /**
     * H2 Console管理者パスワード。
     * application.propertiesを通じて環境変数から取得します。
     */
    @Value("${h2.admin.password}")
    private String h2AdminPassword;

    /**
     * コンストラクタによる依存性注入。
     *
     * @param passwordEncoder パスワードエンコーダー
     */
    public H2UserDetailsService(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * H2 Consoleの管理者認証情報を取得します。
     *
     * @param username ログイン時に入力されたユーザー名
     * @return Spring Securityが認証に使用するUserDetails
     * @throws UsernameNotFoundException 管理者ユーザー以外が指定された場合
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.debug("H2 Consoleの認証処理を開始します。ユーザー名: {}", username);

        // H2 Consoleへのログインはadminユーザーに限定します。
        if (!"admin".equals(username)) {
            log.warn("H2 Consoleの認証に失敗しました。無効なユーザー名です。");
            throw new UsernameNotFoundException("ユーザー名またはパスワードが正しくありません。");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password(passwordEncoder.encode(h2AdminPassword))
                .roles("ADMIN")
                .build();
    }
}
