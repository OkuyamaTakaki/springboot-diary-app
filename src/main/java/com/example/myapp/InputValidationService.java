package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * アプリケーション全体で共通利用する入力値のバリデーションを担当するサービス。
 */
@Service
public class InputValidationService {

    private static final Logger log = LoggerFactory.getLogger(InputValidationService.class);

    /**
     * 指定された文字列が最大文字数以内か確認します。
     *
     * @param value 検証対象の文字列
     * @param maxLength 許可する最大文字数
     * @return nullではなく、最大文字数以内ならtrue
     */
    public boolean isWithinMaxLength(final String value, final int maxLength) {
        if (value == null) {
            log.debug("バリデーションエラー：入力値がnullです。");
            return false;
        }

        if (maxLength < 0) {
            log.warn("不正な最大文字数が指定されました。最大文字数: {}", maxLength);
            return false;
        }

        final boolean valid = value.length() <= maxLength;

        if (!valid) {
            log.debug(
                    "バリデーションエラー：入力値が最大文字数を超えています。実際の文字数: {}, 最大文字数: {}",
                    value.length(),
                    maxLength);
        }

        return valid;
    }
}
