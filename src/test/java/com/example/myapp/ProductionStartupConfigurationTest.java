package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class ProductionStartupConfigurationTest {

    @Test
    void productionAvoidsUnusedDatabaseConnectionsAndKeepsSecurityDefaults() throws Exception {
        final Properties properties = loadProperties("application-prod.properties");

        assertThat(properties)
                .containsEntry("spring.datasource.hikari.minimum-idle", "0")
                .containsEntry("spring.h2.console.enabled", "false")
                .containsEntry("server.servlet.session.cookie.http-only", "true")
                .containsEntry("server.servlet.session.cookie.secure", "true");
    }

    @Test
    void messagesDoNotFallBackToTheServerOperatingSystemLocale() throws Exception {
        final Properties properties = loadProperties("application.properties");

        assertThat(properties)
                .containsEntry("spring.messages.fallback-to-system-locale", "false");
    }

    private Properties loadProperties(final String resourceName) throws Exception {
        final Properties properties = new Properties();
        try (var stream = getClass().getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertThat(stream).as(resourceName).isNotNull();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
