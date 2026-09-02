package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class LocalizationResourceTest {

    @Test
    void japaneseAndEnglishContainTheSameNonEmptyKeys() throws Exception {
        final Properties japanese = load("messages.properties");
        final Properties english = load("messages_en.properties");

        assertThat(english.keySet()).containsExactlyInAnyOrderElementsOf(japanese.keySet());
        assertThat(japanese.values()).allSatisfy(value ->
                assertThat(value.toString()).isNotBlank());
        assertThat(english.values()).allSatisfy(value ->
                assertThat(value.toString()).isNotBlank());
    }

    private Properties load(final String resourceName) throws Exception {
        final Properties properties = new Properties();
        try (var stream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertThat(stream).as(resourceName).isNotNull();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
