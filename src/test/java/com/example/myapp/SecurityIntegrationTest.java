package com.example.myapp;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void protectedPageRedirectsAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void privacyPageIsPublicAndRendersBothLanguages() throws Exception {
        mockMvc.perform(get("/privacy"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("プライバシー情報")))
                .andExpect(content().string(containsString("仮問い合わせ窓口")))
                .andExpect(content().string(containsString("https://github.com/OkuyamaTakaki")))
                .andExpect(content().string(containsString("秘密情報・個人情報は送らないでください")));

        mockMvc.perform(get("/privacy").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Privacy information")))
                .andExpect(content().string(containsString("temporary contact route")))
                .andExpect(content().string(containsString("Do not send passwords")));
    }

    @Test
    void unsafeRequestWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "alice")
                .param("password", "StrongPass!2026"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/register")
                .with(csrf().useInvalidToken())
                .param("username", "alice")
                .param("password", "StrongPass!2026"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/logout").with(user("alice")))
                .andExpect(status().isForbidden());
    }

    @Test
    void databaseConsoleIsDeniedToAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/h2-console/").with(user("alice")))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginResponseIncludesBrowserSecurityHeaders() throws Exception {
        mockMvc.perform(get("/login").header("Origin", "https://evil.example"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Security-Policy",
                        containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string(
                        "Permissions-Policy",
                        containsString("camera=()")))
                .andExpect(header().string("Cross-Origin-Opener-Policy", "same-origin"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void languageSwitchRendersEnglishAndSavesTheChoice() throws Exception {
        mockMvc.perform(get("/login").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gratitude Diary")))
                .andExpect(content().string(containsString("The first load may take")))
                .andExpect(content().string(containsString("Create an account")))
                .andExpect(cookie().value("diary-language", "en"));
    }

    @Test
    void unsupportedLanguageFallsBackToJapaneseAndRepairsTheCookie() throws Exception {
        mockMvc.perform(get("/login").param("lang", "fr"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ありがとう日記")))
                .andExpect(content().string(containsString("初回表示に時間がかかる")))
                .andExpect(content().string(containsString("新規登録はこちら")))
                .andExpect(cookie().value("diary-language", "ja"));
    }

    @Test
    @Transactional
    void authenticatedIndexRendersEnglishWithoutTemplateErrors() throws Exception {
        userRepository.save(new User("localization-user", "unused-test-hash"));

        mockMvc.perform(get("/")
                .with(user("localization-user"))
                .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Diary entries")))
                .andExpect(content().string(containsString("Write an entry")));
    }
}
