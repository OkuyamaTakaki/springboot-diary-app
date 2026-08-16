package com.example.myapp;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 日記本文のバリデーションを担当するサービス。
 */
@Service
public class DiaryValidationService {

    private static final Logger log = LoggerFactory.getLogger(DiaryValidationService.class);

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_CONTENT_LENGTH = 2000;

    /**
     * 日記本文に含まれていることを許可する感謝・ポジティブ表現。
     */
    private static final List<String> GRATITUDE_KEYWORDS = List.of(
            "ありがとう", "ありがと", "有難う", "サンキュー", "さんきゅー", "3k",
            "感謝", "お礼", "御礼", "深謝", "多謝", "謝意",
            "ありがたい", "有り難い", "かたじけない", "恐れ多い",
            "助かった", "助かりました", "助けてもらった", "救われた",
            "恩", "恩義", "義理", "お陰様", "おかげさま", "お陰で", "おかげで",
            "うれしい", "嬉しい", "ウレシイ", "よろこび", "喜び", "喜ぶ",
            "しあわせ", "幸せ", "幸い", "ハッピー", "はっぴー", "幸福",
            "満足", "満たされる", "心地よい", "心地良い", "快適",
            "よかった", "良かった", "最高", "素敵", "すてき", "素晴らしい", "すばらしい",
            "ほっとした", "ホッとした", "安心", "安らぎ", "やすらぎ",
            "癒やされた", "癒された", "癒し", "温かい", "あたたかい", "優しい", "やさしい",
            "褒められた", "ほめられた", "認められた", "評価された", "賞賛",
            "お褒めの言葉", "応援してもらった", "励まされた");

    private final InputValidationService inputValidationService;

    /**
     * 共通入力バリデーションサービスを注入します。
     *
     * @param inputValidationService 共通入力バリデーションサービス
     */
    public DiaryValidationService(final InputValidationService inputValidationService) {
        this.inputValidationService = inputValidationService;
    }

    /**
     * 日記タイトルが許可された文字数以内か確認します。
     *
     * @param title 日記タイトル
     * @return 許可された文字数以内ならtrue
     */
    public boolean isValidTitleLength(final String title) {
        return inputValidationService.isWithinMaxLength(title, MAX_TITLE_LENGTH);
    }

    /**
     * 日記本文が許可された文字数以内か確認します。
     *
     * @param content 日記本文
     * @return 許可された文字数以内ならtrue
     */
    public boolean isValidContentLength(final String content) {
        return inputValidationService.isWithinMaxLength(content, MAX_CONTENT_LENGTH);
    }

    /**
     * 日記本文に感謝・ポジティブ表現が含まれているか判定します。
     *
     * @param content 日記本文
     * @return 対象となる表現が含まれていればtrue、それ以外はfalse
     */
    public boolean containsGratitude(final String content) {
        if (content == null || content.isBlank()) {
            log.debug("バリデーションエラー：日記本文が空です。");
            return false;
        }

        final boolean hasGratitude = GRATITUDE_KEYWORDS.stream()
                .anyMatch(content::contains);

        if (hasGratitude) {
            log.debug("感謝・ポジティブ表現を検出しました。");
        } else {
            log.debug("感謝・ポジティブ表現を検出できませんでした。");
        }

        return hasGratitude;
    }
}