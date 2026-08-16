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
 * 日記の登録、一覧表示、取得、更新を担当するコントローラー。
 */
@Controller
public class DiaryController {

    private static final Logger log = LoggerFactory.getLogger(DiaryController.class);

    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final DiaryValidationService diaryValidationService;

    /**
     * 必要なコンポーネントをコンストラクタから注入します。
     *
     * @param diaryRepository 日記データを操作するリポジトリ
     * @param userService ユーザー情報を取得するサービス
     * @param diaryValidationService 日記内容を検証するサービス
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
     * 登録前に本文のバリデーションを実行し、登録後は共通通知モーダルを表示します。
     *
     * @param title 日記タイトル
     * @param content 日記本文
     * @param authentication ログインユーザー情報
     * @param model 画面表示用データ
     * @param redirectAttributes リダイレクト先へ渡す通知メッセージ
     * @return 遷移先
     */
    @PostMapping("/diary")
    public String diary(
            @RequestParam final String title,
            @RequestParam final String content,
            final Authentication authentication,
            final Model model,
            final RedirectAttributes redirectAttributes) {

        final String username = authentication.getName();
        log.info("日記の新規登録を開始します。ユーザー: {}", username);

        final User user = userService.findByUsername(username);

        if (!diaryValidationService.containsGratitude(content)) {
            log.warn("日記のバリデーションに失敗しました。ユーザー: {}", username);

            model.addAttribute(
                    "errorMessage",
                    "日記には感謝の気持ちを含めてください。\n\n"
                            + "「ありがとう」\n"
                            + "「感謝」\n"
                            + "「ありがたい」\n"
                            + "「お礼」\n"
                            + "「助かった」\n\n"
                            + "のいずれかを含めて書き直してください。");
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            setupIndexModel(model, user, username);

            return "index";
        }

        final Diary diary = new Diary(title, content);
        diary.setUser(user);
        diaryRepository.save(diary);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "日記「" + title + "」を登録しました。");

        log.info("日記の新規登録が完了しました。ユーザー: {}", username);

        return "redirect:/";
    }

    /**
     * メイン画面を表示し、ログインユーザー本人の日記だけを一覧表示します。
     *
     * @param model 画面表示用データ
     * @param authentication ログインユーザー情報
     * @return 日記一覧画面
     */
    @GetMapping("/")
    public String index(
            final Model model,
            final Authentication authentication) {

        final String username = authentication.getName();
        final User user = userService.findByUsername(username);

        log.debug("日記一覧を取得します。ユーザー: {}", username);

        setupIndexModel(model, user, username);

        return "index";
    }

    /**
     * 指定された日記を取得します。
     * ログインユーザー本人の日記以外は取得できないようにします。
     *
     * @param id 日記ID
     * @param authentication ログインユーザー情報
     * @return 日記データ、または404
     */
    @GetMapping("/api/diary/{id}")
    public ResponseEntity<Map<String, Object>> getDiary(
            @PathVariable final Long id,
            final Authentication authentication) {

        final String username = authentication.getName();
        final User user = userService.findByUsername(username);
        final Diary diary = diaryRepository.findById(id).orElse(null);

        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            log.warn(
                    "日記の取得を拒否しました。ID: {}, ユーザー: {}",
                    id,
                    username);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "id", diary.getId(),
                        "title", diary.getTitle(),
                        "content", diary.getContent()));
    }

    /**
     * 指定された日記を更新します。
     * 更新前にも新規登録時と同じバリデーションを実行します。
     *
     * @param id 日記ID
     * @param title 日記タイトル
     * @param content 日記本文
     * @param authentication ログインユーザー情報
     * @return 更新結果
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

        if (diary == null || !diary.getUser().getId().equals(user.getId())) {
            log.warn(
                    "日記の更新を拒否しました。ID: {}, ユーザー: {}",
                    id,
                    username);
            return ResponseEntity.notFound().build();
        }

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

        diary.setTitle(title);
        diary.setContent(content);
        diaryRepository.save(diary);

        log.info(
                "日記の更新が完了しました。ID: {}, ユーザー: {}",
                id,
                username);

        return ResponseEntity.ok("更新しました");
    }

    /**
     * 日記一覧画面に必要なデータをModelへ設定します。
     * ユーザー自身の日記だけを取得することで、他ユーザーの日記が表示されることを防ぎます。
     *
     * @param model 画面表示用データ
     * @param user ログインユーザー
     * @param username ログインユーザー名
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