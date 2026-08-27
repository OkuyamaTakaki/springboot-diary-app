package com.example.myapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * アプリケーションの認証・認可およびセキュリティ設定を管理します。
 */
@Configuration
public class SecurityConfig {

    private static final String CONTENT_SECURITY_POLICY = String.join("; ",
            "default-src 'self'",
            "base-uri 'self'",
            "connect-src 'self'",
            "font-src 'self' https://fonts.gstatic.com",
            "form-action 'self'",
            "frame-ancestors 'none'",
            "img-src 'self' data:",
            "object-src 'none'",
            "script-src 'self'",
            "style-src 'self' https://fonts.googleapis.com");

    /**
     * パスワードをBCryptでハッシュ化します。
     *
     * @return パスワードエンコーダー
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 一般ユーザー向けのメインアプリケーションのセキュリティ設定です。
     *
     * @param http HTTPセキュリティ設定
     * @param customUserDetailsService 一般ユーザー用認証サービス
     * @return メインアプリケーション用セキュリティフィルターチェーン
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final CustomUserDetailsService customUserDetailsService) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/h2-console/**"
                ).denyAll()
                .requestMatchers(
                    "/register",
                    "/health",
                    "/error",
                    "/css/**",
                    "/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp
                    .policyDirectives(CONTENT_SECURITY_POLICY));
                headers.referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
                headers.permissionsPolicy(permissions -> permissions
                    .policy("camera=(), geolocation=(), microphone=(), payment=()"));
                headers.crossOriginOpenerPolicy(opener -> opener
                    .policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN));
            })
            .userDetailsService(customUserDetailsService);

        return http.build();
    }
}
