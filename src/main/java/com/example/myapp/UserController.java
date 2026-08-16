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

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 必要なコンポーネントをコンストラクタから注入します。
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

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            log.warn("登録エラー：ユーザー名またはパスワードが未入力です。");
            model.addAttribute(
                    "errorMessage",
                    "ユーザー名とパスワードは必ず入力してください。");
            return "register";
        }

        if (userService.findByUsername(username) != null) {
            log.warn("登録エラー：ユーザー名がすでに登録されています。ユーザー名: {}", username);

            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute(
                    "registerErrorMessage",
                    "そのユーザー名は既に登録されています。\n別のユーザー名をご利用ください。");

            return "redirect:/register";
        }

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