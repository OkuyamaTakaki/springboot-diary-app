package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 開発環境のH2 Console専用の認証サービス。
 */
@Service
public class H2UserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(H2UserDetailsService.class);

    private final PasswordEncoder passwordEncoder;

    /**
     * H2 Console管理者パスワード。
     * 環境変数 H2_ADMIN_PASSWORD から取得する。
     */
    @Value("${h2.admin.password}")
    private String h2AdminPassword;

    /**
     * コンストラクタ注入。
     */
    public H2UserDetailsService(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.info("H2 Console への接続試行を検知しました。ユーザー名: {}", username);

        // 管理者名が「admin」でなければアクセスを拒否
        if (!"admin".equals(username)) {
            log.warn("H2 Console へのアクセスが拒否されました。無効なユーザー名です: {}", username);
            throw new UsernameNotFoundException("ユーザー名またはパスワードが正しくありません。");
        }

        log.info("H2 Console の管理者認証を処理します。");

        return org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password(passwordEncoder.encode(h2AdminPassword))
                .roles("ADMIN")
                .build();
    }
}