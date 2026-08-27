package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class UserControllerTest {

    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        controller = new UserController(
                userService,
                passwordEncoder,
                new InputValidationService());
    }

    @Test
    void rejectsUsernameLongerThanDatabaseColumn() {
        final ConcurrentModel model = new ConcurrentModel();

        assertThat(controller.register(
                "u".repeat(51),
                "StrongPass!2026",
                model,
                new RedirectAttributesModelMap()))
                .isEqualTo("register");
        assertThat(model.getAttribute("registerErrorMessage"))
                .asString()
                .contains("3〜50文字");
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void rejectsPasswordShorterThanEightCharacters() {
        final ConcurrentModel model = new ConcurrentModel();

        assertThat(controller.register(
                "alice",
                "short",
                model,
                new RedirectAttributesModelMap()))
                .isEqualTo("register");
        assertThat(model.getAttribute("registerErrorMessage"))
                .asString()
                .contains("8文字以上");
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void trimsUsernameBeforeSaving() {
        when(passwordEncoder.encode("StrongPass!2026")).thenReturn("encoded");

        assertThat(controller.register(
                "  alice  ",
                "StrongPass!2026",
                new ConcurrentModel(),
                new RedirectAttributesModelMap()))
                .isEqualTo("redirect:/login");

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("alice");
    }
}
