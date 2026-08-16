package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================================================================
    // 1. H2 Console 専用セキュリティチェーン
    // ==========================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain h2SecurityFilterChain(
            final HttpSecurity http,
            final H2UserDetailsService h2UserDetailsService) throws Exception {

        log.info("H2 Console専用のセキュリティチェーン(Order 1)を構築します。");

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

    // ==========================================================================
    // 2. 一般ユーザー・メインアプリ専用セキュリティチェーン
    // ==========================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final CustomUserDetailsService customUserDetailsService) throws Exception {

        log.info("一般ユーザー専用のメインセキュリティチェーン(Order 2)を構築します。");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/register",
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
            .userDetailsService(customUserDetailsService);

        return http.build();
    }
}