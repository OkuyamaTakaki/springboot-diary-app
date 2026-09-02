package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class PrivacyControllerTest {

    @Test
    void exposesOnlySupportedContactSchemes() {
        final ConcurrentModel httpsModel = new ConcurrentModel();
        new PrivacyController("https://example.com/privacy-contact")
                .privacyPage(httpsModel);

        assertThat(httpsModel.getAttribute("privacyContactConfigured")).isEqualTo(true);
        assertThat(httpsModel.getAttribute("privacyContactUrl"))
                .isEqualTo("https://example.com/privacy-contact");

        final ConcurrentModel unsafeModel = new ConcurrentModel();
        new PrivacyController("javascript:alert(1)").privacyPage(unsafeModel);

        assertThat(unsafeModel.getAttribute("privacyContactConfigured")).isEqualTo(false);
        assertThat(unsafeModel.getAttribute("privacyContactUrl")).isEqualTo("");
    }
}
