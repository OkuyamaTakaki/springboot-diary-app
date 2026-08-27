package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 一般ユーザーの認証処理を担当するサービス。
 * Spring Securityから呼び出され、データベースに登録されたユーザー情報を取得して認証用UserDetailsへ変換します。
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserService userService;

    /**
     * コンストラクタによる依存性注入。
     *
     * @param userService ユーザー情報を取得するサービス
     */
    public CustomUserDetailsService(final UserService userService) {
        this.userService = userService;
    }

    /**
     * ログイン時に入力されたユーザー名から認証対象ユーザーを取得します。
     *
     * @param username ログイン時に入力されたユーザー名
     * @return Spring Securityが認証に使用するUserDetails
     * @throws UsernameNotFoundException ユーザーが存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.debug("一般ユーザーの認証処理を開始します。ユーザー名: {}", username);

        final User user = userService.findByUsername(username);

        // ユーザーが存在しない場合は認証失敗として例外を送出します。
        if (user == null) {
            log.warn("認証に失敗しました。ユーザーが存在しません。ユーザー名: {}", username);
            throw new UsernameNotFoundException("ユーザー名またはパスワードが正しくありません。");
        }

        log.debug("認証対象ユーザーを取得しました。ユーザー名: {}", username);

        // DBに保存されたBCryptハッシュ化パスワードをSpring Securityへ渡し、認証処理を委譲します。
        // 一般ユーザーにはUSERロールを付与します。
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
