package com.example.myapp;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 日記の入力内容に対するバリデーション（検証）ロジックを提供するサービス。
 * 感謝、喜び、思いやりなど、ポジティブな言葉が含まれているかを厳格に判定します。
 */
@Service
public class DiaryValidationService {

    private static final Logger log = LoggerFactory.getLogger(DiaryValidationService.class);

    /**
     * 感謝およびポジティブな感情を表す拡張キーワードリスト。
     * 実務的な網羅性を高めるため、類語・表記ゆれ・日常的な喜びの表現を一挙に追加しています。
     */
    private static final List<String> GRATITUDE_KEYWORDS = List.of(
        // --- 1. 基本的な感謝・お礼 ---
        "ありがとう", "有難う", "サンキュー", "さんきゅー", "3k",
        "感謝", "お礼", "御礼", "深謝", "多謝", "謝意",
        "ありがたい", "有り難い", "かたじけない", "恐れ多い",

        // --- 2. 救われた・助かった表現 ---
        "助かった", "助かりました", "助けてもらった", "救われた", 
        "恩", "恩義", "義理", "お陰様", "おかげさま", "お陰で", "おかげで",

        // --- 3. 嬉しい・幸せ・満足の感情 ---
        "うれしい", "嬉しい", "ウレシイ", "よろこび", "喜び", "喜ぶ",
        "しあわせ", "幸せ", "幸い", "ハッピー", "はっぴー", "幸福",
        "満足", "満たされる", "心地よい", "心地良い", "快適",
        "よかった", "良かった", "最高", "素敵", "すてき", "素晴らしい", "すばらしい",

        // --- 4. 癒やし・安心・ほっとした表現 ---
        "ほっとした", "ホッとした", "安心", "安らぎ", "やすらぎ",
        "癒やされた", "癒された", "癒し", "温かい", "あたたかい", "優しい", "やさしい",

        // --- 5. 褒められた・認められた表現 ---
        "褒められた", "ほめられた", "認められた", "評価された", "賞賛",
        "お褒めの言葉", "応援してもらった", "励まされた"
    );

    /**
     * 日記の本文に、指定されたポジティブな表現のいずれかが含まれているかを判定します。
     * 
     * @param content 日記の本文テキスト
     * @return キーワードが1つでも含まれている場合はtrue、含まれていない場合や入力が空の場合はfalse
     */
    public boolean containsGratitude(final String content) {
        if (content == null || content.isBlank()) {
            log.debug("バリデーションスキップ：本文が空、または空白です。");
            return false;
        }

        log.debug("拡張感謝キーワードチェックを開始します。本文長: {} 文字", content.length());

        // Stream APIによる高速な部分一致判定（どれか1つでもヒットすれば即座に終了する最適化が効きます）
        final boolean hasGratitude = GRATITUDE_KEYWORDS.stream()
                .anyMatch(content::contains);

        if (hasGratitude) {
            log.info("感謝・ポジティブキーワードの検出に成功しました。");
        } else {
            log.warn("バリデーションエラー：感謝・ポジティブキーワードが日記本文に含まれていません。");
        }

        return hasGratitude;
    }
}