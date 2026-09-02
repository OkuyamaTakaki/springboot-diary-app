package com.example.myapp;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 認証前でも確認できるプライバシー情報を表示します。
 */
@Controller
public class PrivacyController {

    private static final Pattern SAFE_CONTACT_URL = Pattern.compile(
            "^(?:https://|mailto:)[^\\s]+$",
            Pattern.CASE_INSENSITIVE);

    private final String privacyContactUrl;

    /**
     * 公開用の連絡先はソースへ個人情報を固定せず、実行環境から受け取ります。
     * 不正または未設定の値はリンクとして出力しません。
     *
     * @param configuredContactUrl 問い合わせ先のHTTPS URLまたはmailto URL
     */
    public PrivacyController(
            @Value("${app.privacy.contact-url:}") final String configuredContactUrl) {
        this.privacyContactUrl = normalizeContactUrl(configuredContactUrl);
    }

    /**
     * プライバシー情報を表示します。
     *
     * @param model テンプレートへ渡す表示情報
     * @return プライバシー画面
     */
    @GetMapping("/privacy")
    public String privacyPage(final Model model) {
        model.addAttribute("privacyContactUrl", privacyContactUrl);
        model.addAttribute("privacyContactConfigured", !privacyContactUrl.isBlank());
        return "privacy";
    }

    private static String normalizeContactUrl(final String configuredContactUrl) {
        if (configuredContactUrl == null) {
            return "";
        }

        final String normalizedUrl = configuredContactUrl.strip();
        return SAFE_CONTACT_URL.matcher(normalizedUrl).matches() ? normalizedUrl : "";
    }
}
