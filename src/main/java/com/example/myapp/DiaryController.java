package com.example.myapp;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 日記の登録・一覧表示・編集に関するHTTPリクエストを処理するコントローラー。
 *
 * Thymeleafによる画面表示と、JavaScriptから利用する非同期APIを提供します。
 */
@Controller
public class DiaryController {

    private static final Logger log = LoggerFactory.getLogger(DiaryController.class);

    /**
     * 日記データへのアクセスを担当するリポジトリ。
     */
    private final DiaryRepository diaryRepository;

    /**
     * ユーザー情報の取得・登録を担当するサービス。
     */
    private final UserService userService;

    /**
     * 日記本文のバリデーションを担当するサービス。
     */
    private final DiaryValidationService diaryValidationService;

    /**
     * 必要な依存コンポーネントをコンストラクタから注入します。
     */
    public DiaryController(
            final DiaryRepository diaryRepository,
            final UserService userService,
            final DiaryValidationService diaryValidationService) {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
        this.diaryValidationService = diaryValidationService;
    }

    /**
     * 新規日記を登録します。
     *
     * 入力内容をバリデーションし、問題がなければデータベースへ保存します。
     * 登録成功時はFlash Attributeでメッセージを保持して一覧画面へリダイレクトします。
     */
    @PostMapping("/diary")
    public String diary(
            @RequestParam final String title,
            @RequestParam final String content,
            final Authentication authentication,
            final Model model,
            final RedirectAttributes redirectAttributes) {

        final String username = authentication.getName();
        log.info("日記の新規登録リクエストを受信しました。ユーザー: {}", username);

        final User user = userService.findByUsername(username);

        // 日記本文に必要な感謝表現が含まれているか検証します。
        if (!diaryValidationService.containsGratitude(content)) {
            log.warn("日記のバリデーションエラー（感謝の言葉が不足）。ユーザー: {}", username);

            model.addAttribute(
                    "errorMessage",
                    "日記には感謝の気持ちを含めてください。\n\n"
                            + "「ありがとう」\n"
                            + "「感謝」\n"
                            + "「ありがたい」\n"
                            + "「お礼」\n"
                            + "「助かった」\n\n"
                            + "のいずれかを含めて書き直してください。");

            // バリデーションエラー時も入力内容を保持します。
            model.addAttribute("title", title);
            model.addAttribute("content", content);

            // エラー画面でも日記一覧などの共通データを表示できるようにします。
            setupIndexModel(model, user, username);

            return "index";
        }

        // 新しい日記を作成し、ログイン中のユーザーを所有者として設定します。
        final Diary diary = new Diary(title, content);
        diary.setUser(user);

        // 日記をデータベースへ保存します。
        diaryRepository.save(diary);

        // 登録成功メッセージをFlash Attributeへ設定します。
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "日記「" + title + "」を登録しました。");

        log.info("日記の登録が正常に完了しました。リダイレクトします。");

        return "redirect:/";
    }

    /**
     * メイン画面（日記一覧）を表示します。
     */
    @GetMapping("/")
    public String index(
            final Model model,
            final Authentication authentication) {

        final String username = authentication.getName();
        log.info("マイページ一覧画面へのアクセス。ユーザー: {}", username);

        final User user = userService.findByUsername(username);
        setupIndexModel(model, user, username);

        return "index";
    }

    /**
     * 指定された日記を非同期で取得します。
     *
     * ログインユーザー本人の日記だけを取得できるようアクセス制御します。
     */
    @GetMapping("/api/diary/{id}")
    public ResponseEntity<Map<String, Object>> getDiary(
            @PathVariable final Long id,
            final Authentication authentication) {

        final String username = authentication.getName();
        final User user = userService.findByUsername(username);
        final Diary diary = diaryRepository.findById(id).orElse(null);

        // 日記が存在しない、または本人の日記ではない場合は404を返します。
        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            log.warn(
                    "不正な日記データ取得を検知、またはデータが存在しません。ID: {}, ユーザー: {}",
                    id,
                    username);
            return ResponseEntity.notFound().build();
        }

        // 編集画面に必要な項目だけをJSONとして返します。
        return ResponseEntity.ok(
                Map.of(
                        "id", diary.getId(),
                        "title", diary.getTitle(),
                        "content", diary.getContent()));
    }

    /**
     * 指定された日記を非同期で更新します。
     *
     * 更新前にも新規登録時と同じバリデーションを実行します。
     */
    @PostMapping("/api/diary/{id}")
    public ResponseEntity<String> updateDiary(
            @PathVariable final Long id,
            @RequestParam final String title,
            @RequestParam final String content,
            final Authentication authentication) {

        final String username = authentication.getName();
        final User user = userService.findByUsername(username);
        final Diary diary = diaryRepository.findById(id).orElse(null);

        // 日記が存在しない、または本人の日記ではない場合は404を返します。
        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            log.warn(
                    "不正な日記データ更新要求を検知しました。ID: {}, ユーザー: {}",
                    id,
                    username);
            return ResponseEntity.notFound().build();
        }

        // 更新時にも感謝表現のバリデーションを実行します。
        if (!diaryValidationService.containsGratitude(content)) {
            return ResponseEntity.badRequest().body(
                    "日記には感謝の気持ちを含めてください。\n\n"
                            + "「ありがとう」\n"
                            + "「感謝」\n"
                            + "「ありがたい」\n"
                            + "「お礼」\n"
                            + "「助かった」\n\n"
                            + "のいずれかを含めて書き直してください。");
        }

        // 日記のタイトルと本文を更新します。
        diary.setTitle(title);
        diary.setContent(content);

        // 更新日時はDiaryエンティティ側で自動的に更新されます。
        diaryRepository.save(diary);

        log.info("日記（ID: {}）が正常に更新されました。", id);

        return ResponseEntity.ok("更新しました");
    }

    /**
     * 日記一覧画面に必要な共通データをModelへ設定します。
     *
     * ログインユーザー本人の日記だけを取得することで、
     * 他ユーザーの日記が表示されないようにします。
     */
    private void setupIndexModel(
            final Model model,
            final User user,
            final String username) {

        final List<Diary> diaries = diaryRepository.findByUser(user);

        model.addAttribute("diaries", diaries);
        model.addAttribute("username", username);
    }
}