package com.example.myapp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

/**
 * アプリケーションの認証・認可およびセキュリティ設定を管理します。
 */
@Configuration
public class SecurityConfig {

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
     * H2 Console専用のセキュリティ設定です。
     *
     * @param http HTTPセキュリティ設定
     * @param h2UserDetailsService H2 Console用認証サービス
     * @return H2 Console用セキュリティフィルターチェーン
     */
    @Bean
    @Order(1)
    @ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
    public SecurityFilterChain h2SecurityFilterChain(
            final HttpSecurity http,
            final H2UserDetailsService h2UserDetailsService) throws Exception {

        http
            .securityMatcher("/h2-console/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasRole("ADMIN")
            )
            .httpBasic(httpBasic -> {})
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .userDetailsService(h2UserDetailsService);

        return http.build();
    }

    /**
     * 一般ユーザー向けのメインアプリケーションのセキュリティ設定です。
     *
     * @param http HTTPセキュリティ設定
     * @param customUserDetailsService 一般ユーザー用認証サービス
     * @return メインアプリケーション用セキュリティフィルターチェーン
     */
    @Bean
    @Order(2)
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
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .userDetailsService(customUserDetailsService);

        return http.build();
    }
}
