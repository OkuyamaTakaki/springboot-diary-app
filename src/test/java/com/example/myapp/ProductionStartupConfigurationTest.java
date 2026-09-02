package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class ProductionStartupConfigurationTest {

    @Test
    void productionAvoidsUnusedDatabaseConnectionsAndKeepsSecurityDefaults() throws Exception {
        final Properties properties = loadProductionProperties();

        assertThat(properties)
                .containsEntry("spring.datasource.hikari.minimum-idle", "0")
                .containsEntry("spring.h2.console.enabled", "false")
                .containsEntry("server.servlet.session.cookie.http-only", "true")
                .containsEntry("server.servlet.session.cookie.secure", "true");
    }

    private Properties loadProductionProperties() throws Exception {
        final Properties properties = new Properties();
        try (var stream = getClass().getClassLoader()
                .getResourceAsStream("application-prod.properties")) {
            assertThat(stream).as("application-prod.properties").isNotNull();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
