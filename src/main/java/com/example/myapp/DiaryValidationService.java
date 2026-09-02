package com.example.myapp;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
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

    private static final Pattern NEGATION_AFTER_KEYWORD = Pattern.compile(
            "^(?:表現)?(?:を|は|が|なんて|とは|だと|だなんて|の気持ちを)?"
                    + "[^。.!！?？\\n]{0,10}"
                    + "(?:含ま(?:ない|ず)|し(?:ない|ていない|ていません|ません)|思わ(?:ない|ず)|"
                    + "感じ(?:ない|ていない|ません)|言わ(?:ない|ず)|伝え(?:ない|ていない|ません)|"
                    + "要らない|不要|なし|無い|ない|ありません|否定)");

    private static final Pattern ENGLISH_NEGATION_BEFORE_KEYWORD = Pattern.compile(
            "(?:\\b(?:not|never|without|nobody)\\s+|"
                    + "\\b(?:no[ -]?one)\\s+|"
                    + "\\b(?:do|does|did|am|is|are|was|were|can|could|would|should|will)"
                    + "\\s+not\\s+|"
                    + "\\b(?:don't|doesn't|didn't|isn't|aren't|wasn't|weren't|can't|"
                    + "couldn't|wouldn't|shouldn't|won't)\\s+)"
                    + "[^.!?\\n]{0,16}$");

    /**
     * 英語の候補語だけに単語境界を適用するための判定。
     * 日本語には活用や後続語があるため、同じ境界規則を強制しない。
     */
    private static final Pattern ENGLISH_KEYWORD = Pattern.compile(
            "[a-z]+(?: [a-z]+)*");

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
            "お褒めの言葉", "応援してもらった", "励まされた",
            "thank you", "thanks", "thankful", "grateful", "gratitude",
            "appreciate", "appreciated", "helped", "kindness", "fortunate");

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

        final String normalized = Normalizer.normalize(content, Normalizer.Form.NFKC)
                .replace('’', '\'')
                .toLowerCase(Locale.ROOT);
        final boolean hasGratitude = GRATITUDE_KEYWORDS.stream()
                .map(keyword -> Normalizer.normalize(keyword, Normalizer.Form.NFKC)
                        .toLowerCase(Locale.ROOT))
                .anyMatch(keyword -> containsNonNegatedKeyword(normalized, keyword));

        if (hasGratitude) {
            log.debug("感謝・ポジティブ表現を検出しました。");
        } else {
            log.debug("感謝・ポジティブ表現を検出できませんでした。");
        }

        return hasGratitude;
    }

    /**
     * キーワードの各出現箇所を調べ、直後の文脈で否定されていないものがあるか確認する。
     */
    private boolean containsNonNegatedKeyword(final String content, final String keyword) {
        int fromIndex = 0;
        while (fromIndex < content.length()) {
            final int keywordIndex = content.indexOf(keyword, fromIndex);
            if (keywordIndex < 0) {
                return false;
            }

            final int nextFromIndex = keywordIndex + keyword.length();
            if (ENGLISH_KEYWORD.matcher(keyword).matches()
                    && !hasEnglishWordBoundaries(content, keywordIndex, nextFromIndex)) {
                fromIndex = nextFromIndex;
                continue;
            }

            final int contextStart = nextFromIndex;
            final int contextEnd = Math.min(content.length(), contextStart + 24);
            final String followingContext = content.substring(contextStart, contextEnd);
            final int precedingStart = Math.max(0, keywordIndex - 32);
            final String precedingContext = content.substring(precedingStart, keywordIndex);
            if (!NEGATION_AFTER_KEYWORD.matcher(followingContext).find()
                    && !ENGLISH_NEGATION_BEFORE_KEYWORD.matcher(precedingContext).find()) {
                return true;
            }

            fromIndex = nextFromIndex;
        }
        return false;
    }

    /**
     * 英語キーワードが別の単語の一部ではなく、独立した語または句か確認する。
     */
    private boolean hasEnglishWordBoundaries(
            final String content,
            final int keywordStart,
            final int keywordEnd) {

        final boolean startsInsideWord = keywordStart > 0
                && isWordCharacter(content.charAt(keywordStart - 1));
        final boolean endsInsideWord = keywordEnd < content.length()
                && isWordCharacter(content.charAt(keywordEnd));
        return !startsInsideWord && !endsInsideWord;
    }

    private boolean isWordCharacter(final char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }
}
