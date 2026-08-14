package com.example.myapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // H2 Console専用
    @Bean
    @Order(1)
    public SecurityFilterChain h2SecurityFilterChain(
            HttpSecurity http,
            H2UserDetailsService h2UserDetailsService) throws Exception {

        http
            .securityMatcher("/h2-console/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasRole("ADMIN")
            )
            .httpBasic(httpBasic -> {})
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )
            .userDetailsService(h2UserDetailsService);

        return http.build();
    }

    // 一般ユーザー専用
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService customUserDetailsService) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/error").permitAll()
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