package com.example.myapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class H2UserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    @Value("${h2.admin.password}")
    private String adminPassword;

    public H2UserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (!username.equals("admin")) {
            throw new UsernameNotFoundException("ユーザーが見つかりません");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
    }
}