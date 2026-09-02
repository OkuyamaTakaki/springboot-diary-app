package com.example.myapp;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ログイン・ユーザー登録画面とユーザー登録処理を担当するコントローラー。
 */
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 72;
    private static final int PASSWORD_MAX_BYTES = 72;
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "[\\p{L}\\p{N}._-]{3,50}");

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final InputValidationService inputValidationService;
    private final LocalizedMessages messages;

    /**
     * 必要なコンポーネントをコンストラクタから注入します。
     *
     * @param userService ユーザー情報を操作するサービス
     * @param passwordEncoder パスワードを暗号化するエンコーダー
     * @param inputValidationService 共通入力バリデーションサービス
     * @param messages 利用者の選択言語に対応するメッセージ
     */
    public UserController(
            final UserService userService,
            final PasswordEncoder passwordEncoder,
            final InputValidationService inputValidationService,
            final LocalizedMessages messages) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.inputValidationService = inputValidationService;
        this.messages = messages;
    }

    /**
     * ログイン画面を表示します。
     *
     * @return ログイン画面
     */
    @GetMapping("/login")
    public String loginForm() {
        log.debug("ログイン画面を表示します。");
        return "login";
    }

    /**
     * ユーザー登録画面を表示します。
     *
     * @return ユーザー登録画面
     */
    @GetMapping("/register")
    public String registerForm() {
        log.debug("ユーザー登録画面を表示します。");
        return "register";
    }

    /**
     * 新規ユーザーを登録します。
     * 形式・長さをサーバー側でも検証し、BCryptの安全な入力上限を守ります。
     *
     * @param username 登録するユーザー名
     * @param password 登録するパスワード
     * @param model エラーメッセージの格納先
     * @param redirectAttributes リダイレクト先へのメッセージ格納先
     * @return 遷移先
     */
    @PostMapping("/register")
    public String register(
            @RequestParam final String username,
            @RequestParam final String password,
            final Model model,
            final RedirectAttributes redirectAttributes) {

        log.info("ユーザー登録処理を開始します。");

        // 空欄入力を拒否します。
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            log.warn("登録エラー：ユーザー名またはパスワードが未入力です。");
            model.addAttribute(
                    "registerErrorMessage",
                    messages.get("register.error.required"));
            model.addAttribute("username", username);
            return "register";
        }

        final String normalizedUsername = username.strip();

        // 制御文字や空白を含む名前を拒否し、DB列と同じ文字数で検証します。
        if (normalizedUsername.length() < USERNAME_MIN_LENGTH
                || !inputValidationService.isWithinMaxLength(
                        normalizedUsername, USERNAME_MAX_LENGTH)
                || !USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            log.warn("登録エラー：ユーザー名が利用条件を満たしていません。");

            model.addAttribute(
                    "registerErrorMessage",
                    messages.get("register.error.username"));
            model.addAttribute("username", normalizedUsername);
            return "register";
        }

        final int passwordByteLength = password.getBytes(StandardCharsets.UTF_8).length;
        if (password.length() < PASSWORD_MIN_LENGTH
                || !inputValidationService.isWithinMaxLength(
                        password, PASSWORD_MAX_LENGTH)
                || passwordByteLength > PASSWORD_MAX_BYTES) {
            log.warn("登録エラー：パスワードが利用条件を満たしていません。");
            model.addAttribute(
                    "registerErrorMessage",
                    messages.get("register.error.password"));
            model.addAttribute("username", normalizedUsername);
            return "register";
        }

        // 同じユーザー名が登録済みでないか確認します。
        if (userService.findByUsername(normalizedUsername) != null) {
            log.warn("登録エラー：ユーザー名がすでに登録されています。");

            redirectAttributes.addFlashAttribute("username", normalizedUsername);
            redirectAttributes.addFlashAttribute(
                    "registerErrorMessage",
                    messages.get("register.error.duplicate"));

            return "redirect:/register";
        }

        // パスワードはデータベースへ保存する前にハッシュ化します。
        final String encryptedPassword = passwordEncoder.encode(password);
        final User user = new User(normalizedUsername, encryptedPassword);

        try {
            userService.saveUser(user);
        } catch (DataIntegrityViolationException exception) {
            log.warn("登録エラー：同じユーザー名が同時に登録されました。");
            redirectAttributes.addFlashAttribute("username", normalizedUsername);
            redirectAttributes.addFlashAttribute(
                    "registerErrorMessage",
                    messages.get("register.error.duplicate"));
            return "redirect:/register";
        }

        log.info("ユーザー登録が完了しました。");

        redirectAttributes.addFlashAttribute(
                "successMessage",
                messages.get("register.success", normalizedUsername));

        return "redirect:/login";
    }
}
