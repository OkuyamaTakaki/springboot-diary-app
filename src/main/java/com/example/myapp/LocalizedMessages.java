package com.example.myapp;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 現在の表示言語に対応する利用者向けメッセージを取得します。
 */
@Component
public class LocalizedMessages {

    private final MessageSource messageSource;

    public LocalizedMessages(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * リクエストの言語と置換値を使ってメッセージを取得します。
     *
     * @param code messagesファイルのキー
     * @param arguments メッセージへ埋め込む値
     * @return 翻訳済みメッセージ
     */
    public String get(final String code, final Object... arguments) {
        return messageSource.getMessage(
                code,
                arguments,
                LocaleContextHolder.getLocale());
    }
}
