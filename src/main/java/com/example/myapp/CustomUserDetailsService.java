package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 一般ユーザー用の認証処理を行うセキュリティサービス。
 * Spring Securityから呼び出され、データベースのユーザー情報と照合します。
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 実務必須：System.out.printlnを廃止し、本番環境のログ管理システムに連動するSLF4Jロガーを使用
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserService userService;

    /**
     * コンストラクタ注入（推奨される依存性注入の形）。
     * @param userService ユーザーデータを操作するビジネスロジック
     */
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * ログインフォームから入力されたユーザー名をもとに、認証用ユーザー情報を取得します。
     * 
     * @param username ログイン試行されたユーザー名
     * @return Spring Securityが解釈できるUserDetailsオブジェクト
     * @throws UsernameNotFoundException ユーザー名が存在しない場合にスローされる例外
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        // デバッグログ：本番環境では不要な出力レベルを制御できるようdebugまたはinfoで管理
        log.info("ログイン認証を開始しました。検索対象ユーザー名: {}", username);

        // データベースからユーザーエンティティを取得
        final User user = userService.findByUsername(username);

        // ユーザーが存在しない場合は、即座に例外をスロー（ガード節によるネストの浅い綺麗な設計）
        if (user == null) {
            log.warn("ログイン認証に失敗しました。ユーザー名が存在しません: {}", username);
            throw new UsernameNotFoundException("ユーザー名またはパスワードが正しくありません。");
        }

        log.info("ユーザーの取得に成功しました。内部ユーザー情報: {}", user);

        // 同名クラス（com.example.myapp.User）との衝突を防ぐため、Spring SecurityのUserを型指定付きでビルダー生成
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER") // 認可情報（Role）を一律で一般ユーザー権限に設定
                .build();
    }
}