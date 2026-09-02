package com.example.myapp;

import java.time.Duration;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * 日本語・英語の表示切替と、利用者が選んだ言語の保存方法を定義します。
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    private static final String LANGUAGE_PARAMETER = "lang";
    private static final String LANGUAGE_COOKIE = "diary-language";

    /**
     * 初回は日本語を表示し、明示的な言語選択を1年間Cookieへ保存します。
     * Cookieには言語コードだけを格納し、個人情報は含めません。
     *
     * @return 言語設定を解決するリゾルバー
     */
    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver resolver = new CookieLocaleResolver(LANGUAGE_COOKIE) {
            @Override
            protected Locale parseLocaleValue(final String localeValue) {
                return toSupportedLocale(localeValue);
            }
        };
        resolver.setDefaultLocale(Locale.JAPANESE);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookiePath("/");
        resolver.setCookieSameSite("Lax");
        return resolver;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor() {
            @Override
            protected Locale parseLocaleValue(final String localeValue) {
                return toSupportedLocale(localeValue);
            }
        };
        interceptor.setParamName(LANGUAGE_PARAMETER);
        interceptor.setIgnoreInvalidLocale(true);
        registry.addInterceptor(interceptor);
    }

    /**
     * 対応言語を日本語と英語に限定し、不明な値では日本語へ安全に戻します。
     * HTMLのlang属性と実際の表示言語が食い違わないための境界です。
     *
     * @param localeValue URLまたはCookieから受け取った言語コード
     * @return 対応済みのLocale
     */
    private static Locale toSupportedLocale(final String localeValue) {
        return Locale.ENGLISH.getLanguage().equalsIgnoreCase(localeValue)
                ? Locale.ENGLISH
                : Locale.JAPANESE;
    }
}
