package com.example.myapp;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class DiaryController {

    private final DiaryRepository diaryRepository;
    private final UserService userService;

    public DiaryController(
            DiaryRepository diaryRepository,
            UserService userService) {

        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    @PostMapping("/diary")
    public String diary(
            @RequestParam String title,
            @RequestParam String content,
            Authentication authentication) {

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        Diary diary = new Diary(title, content);

        diary.setUser(user);

        diaryRepository.save(diary);

        return "redirect:/";
    }

    @GetMapping("/")
    public String index(
            Model model,
            Authentication authentication) {

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        model.addAttribute(
                "diaries",
                diaryRepository.findByUser(user)
        );

        model.addAttribute("username", username);

        return "index";
    }
}