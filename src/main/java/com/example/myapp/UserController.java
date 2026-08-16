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
 * ログイン・新規登録画面の表示とユーザー登録処理を担当するコントローラー。
 */
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 必要なサービスをコンストラクタから注入します。
     */
    public UserController(
            final UserService userService,
            final PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ログイン画面を表示します。
     *
     * @return ログイン画面
     */
    @GetMapping("/login")
    public String loginForm() {
        log.debug("ログイン画面の描画リクエストを受信しました。");
        return "login";
    }

    /**
     * 新規登録画面を表示します。
     *
     * @return 新規登録画面
     */
    @GetMapping("/register")
    public String registerForm() {
        log.debug("新規会員登録画面の描画リクエストを受信しました。");
        return "register";
    }

    /**
     * 新規ユーザーを登録します。
     *
     * 登録成功時はログイン画面へ、
     * ユーザー名が重複している場合は新規登録画面へ戻します。
     *
     * @param username 登録するユーザー名
     * @param password 登録するパスワード
     * @param model 入力エラーを画面へ渡すためのModel
     * @param redirectAttributes リダイレクト先へメッセージを渡すための属性
     * @return 遷移先の画面
     */
    @PostMapping("/register")
    public String register(
            @RequestParam final String username,
            @RequestParam final String password,
            final Model model,
            final RedirectAttributes redirectAttributes) {

        log.info("新規ユーザーの登録処理を開始します。ユーザー名: {}", username);

        // ユーザー名とパスワードの必須入力を確認します。
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            log.warn("登録エラー：ユーザー名またはパスワードが未入力です。");
            model.addAttribute(
                    "errorMessage",
                    "ユーザー名とパスワードは必ず入力してください。");
            return "register";
        }

        // 同じユーザー名が登録済みか確認します。
        if (userService.findByUsername(username) != null) {
            log.warn("登録エラー：ユーザー名がすでに登録されています: {}", username);

            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute(
                    "registerErrorMessage",
                    "そのユーザー名は既に登録されています。\n別のユーザー名をご利用ください。");

            return "redirect:/register";
        }

        // パスワードをハッシュ化してからユーザー情報を作成します。
        final String encryptedPassword = passwordEncoder.encode(password);
        final User user = new User(username, encryptedPassword);

        // ユーザー情報をデータベースへ保存します。
        userService.saveUser(user);

        log.info("ユーザー登録が完了しました。ユーザー名: {}", username);

        // 登録したユーザー名を含む成功メッセージをログイン画面へ渡します。
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "ユーザー名「" + username + "」を登録しました。");

        return "redirect:/login";
    }
}