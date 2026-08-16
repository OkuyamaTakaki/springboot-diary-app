package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final int USERNAME_MAX_LENGTH = 100;
    private static final int PASSWORD_MAX_LENGTH = 100;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final InputValidationService inputValidationService;

    /**
     * 必要なコンポーネントをコンストラクタから注入します。
     *
     * @param userService ユーザー情報を操作するサービス
     * @param passwordEncoder パスワードを暗号化するエンコーダー
     * @param inputValidationService 共通入力バリデーションサービス
     */
    public UserController(
            final UserService userService,
            final PasswordEncoder passwordEncoder,
            final InputValidationService inputValidationService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.inputValidationService = inputValidationService;
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
     * 空欄チェックと文字数チェックをサーバー側で実行します。
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

        log.info("ユーザー登録処理を開始します。ユーザー名: {}", username);

        // 空欄入力を拒否します。
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            log.warn("登録エラー：ユーザー名またはパスワードが未入力です。");
            model.addAttribute(
                    "registerErrorMessage",
                    "ユーザー名とパスワードは必ず入力してください。");
            model.addAttribute("username", username);
            return "register";
        }

        // ユーザー名とパスワードの最大文字数をサーバー側で検証します。
        if (!inputValidationService.isWithinMaxLength(
                username, USERNAME_MAX_LENGTH)
                || !inputValidationService.isWithinMaxLength(
                        password, PASSWORD_MAX_LENGTH)) {

            log.warn("登録エラー：入力文字数が上限を超えています。");

            model.addAttribute(
                    "registerErrorMessage",
                    "入力できる文字数を超えています。\n\n"
                            + "ユーザー名：100文字以内\n"
                            + "パスワード：100文字以内");
            model.addAttribute("username", username);
            return "register";
        }

        // 同じユーザー名が登録済みでないか確認します。
        if (userService.findByUsername(username) != null) {
            log.warn(
                    "登録エラー：ユーザー名がすでに登録されています。ユーザー名: {}",
                    username);

            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute(
                    "registerErrorMessage",
                    "そのユーザー名は既に登録されています。\n"
                            + "別のユーザー名をご利用ください。");

            return "redirect:/register";
        }

        // パスワードはデータベースへ保存する前にハッシュ化します。
        final String encryptedPassword = passwordEncoder.encode(password);
        final User user = new User(username, encryptedPassword);

        userService.saveUser(user);

        log.info("ユーザー登録が完了しました。ユーザー名: {}", username);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "ユーザー名「" + username + "」を登録しました。");

        return "redirect:/login";
    }
}