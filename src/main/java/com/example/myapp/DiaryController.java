package com.example.myapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /** 1ページあたりに表示する日記の件数。 */
    private static final int DIARIES_PER_PAGE = 7;

    /** 日記の並べ替え項目。 */
    private static final String SORT_FIELD = "updatedAt";

    /** デフォルトの並べ替え方向。 */
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final DiaryValidationService diaryValidationService;

    /**
     * 必要なコンポーネントをコンストラクタから注入する。
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
     * 新規日記を登録する。
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

        // 新規登録時も編集時も同じ文字数制限を適用する。
        if (!diaryValidationService.isValidTitleLength(title)
                || !diaryValidationService.isValidContentLength(content)) {

            log.warn("日記の文字数制限に違反しました。ユーザー: {}", username);

            model.addAttribute(
                    "errorMessage",
                    "入力できる文字数を超えています。\n\n"
                            + "タイトル：100文字以内\n"
                            + "本文：2000文字以内");

            model.addAttribute("title", title);
            model.addAttribute("content", content);
            setupIndexModel(model, user, username);

            return "index";
        }

        // 日記本文に感謝・ポジティブ表現が含まれているか確認する。
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
     * メイン画面を表示する。
     * ログインユーザー本人の日記だけを一覧表示する。
     *
     * @param page 表示するページ番号
     * @param sort 並べ替え方向
     * @param model 画面表示用データ
     * @param authentication ログインユーザー情報
     * @return 日記一覧画面
     */
    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "desc") final String sort,
            @RequestParam(required = false) final String searchDate,
            final Model model,
            final Authentication authentication) {

        final String username = authentication.getName();
        final User user = userService.findByUsername(username);

        log.debug(
                "日記一覧を取得します。ユーザー: {}, ページ: {}, 並べ替え: {}, 検索日: {}",
                username,
                page,
                sort,
                searchDate);

        setupIndexModel(model, user, username, page, sort, searchDate);

        return "index";
    }

    /**
     * 指定された日記を取得する。
     * ログインユーザー本人の日記以外は取得できないようにする。
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
     * 指定された日記を更新する。
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

        // 新規登録時と同じ文字数制限を編集時にも適用する。
        if (!diaryValidationService.isValidTitleLength(title)
                || !diaryValidationService.isValidContentLength(content)) {

            log.warn(
                    "日記の文字数制限に違反しました。ID: {}, ユーザー: {}",
                    id,
                    username);

            return ResponseEntity.badRequest().body(
                    "入力できる文字数を超えています。\n\n"
                            + "タイトル：100文字以内\n"
                            + "本文：2000文字以内");
        }

        // 新規登録時と同じ感謝・ポジティブ表現のチェックを行う。
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
     * 日記一覧画面に必要なデータをModelへ設定する。
     *
     * @param model 画面表示用データ
     * @param user ログインユーザー
     * @param username ログインユーザー名
     * @param page 表示するページ番号
     * @param sort 並べ替え方向
     */
    private void setupIndexModel(
            final Model model,
            final User user,
            final String username,
            final int page,
            final String sort,
            final String searchDate) {

        final int safePage = Math.max(page, 0);
        final Sort.Direction direction = parseSortDirection(sort);
        final Pageable pageable = PageRequest.of(
                safePage,
                DIARIES_PER_PAGE,
                Sort.by(direction, SORT_FIELD));

        final LocalDate searchLocalDate = parseSearchDate(searchDate);
        final Page<Diary> diaryPage;

        if (searchLocalDate != null) {
            final LocalDateTime startOfDay = searchLocalDate.atStartOfDay();
            final LocalDateTime startOfNextDay = searchLocalDate.plusDays(1).atStartOfDay();
            diaryPage = diaryRepository.findByUserAndUpdatedAtBetween(
                    user,
                    startOfDay,
                    startOfNextDay,
                    pageable);
        } else {
            diaryPage = diaryRepository.findByUser(user, pageable);
        }

        model.addAttribute("diaries", diaryPage.getContent());
        model.addAttribute("currentPage", diaryPage.getNumber());
        model.addAttribute("totalPages", diaryPage.getTotalPages());
        model.addAttribute("username", username);
        model.addAttribute("sort", direction.name().toLowerCase());
        model.addAttribute("searchDate", searchDate);
    }

    /**
     * 日付検索用の入力文字列をLocalDateへ変換する。
     *
     * @param searchDate 検索日付
     * @return 変換後のLocalDate。入力がない場合はnull
     */
    private LocalDate parseSearchDate(final String searchDate) {
        if (searchDate == null || searchDate.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(searchDate);
        } catch (final Exception e) {
            log.warn("検索日付の解析に失敗しました。値: {}", searchDate);
            return null;
        }
    }

    /**
     * 並べ替え方向を取得する。
     * 不正な値が指定された場合はデフォルトの降順を使用する。
     *
     * @param sort 並べ替え方向
     * @return 並べ替え方向
     */
    private Sort.Direction parseSortDirection(final String sort) {

        if ("asc".equalsIgnoreCase(sort)) {
            return Sort.Direction.ASC;
        }

        return DEFAULT_SORT_DIRECTION;
    }

    /**
     * バリデーションエラー時の日記一覧を表示する。
     *
     * @param model 画面表示用データ
     * @param user ログインユーザー
     * @param username ログインユーザー名
     */
    private void setupIndexModel(
            final Model model,
            final User user,
            final String username) {

        setupIndexModel(
                model,
                user,
                username,
                0,
                DEFAULT_SORT_DIRECTION.name().toLowerCase(),
                null);
    }
}