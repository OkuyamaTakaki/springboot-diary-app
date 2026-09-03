package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;

class DiaryControllerTest {

    private DiaryRepository diaryRepository;
    private UserService userService;
    private DiaryController controller;
    private Authentication authentication;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        diaryRepository = mock(DiaryRepository.class);
        userService = mock(UserService.class);
        authentication = mock(Authentication.class);
        LocaleContextHolder.setLocale(Locale.JAPANESE);
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        controller = new DiaryController(
                diaryRepository,
                userService,
                new DiaryValidationService(new InputValidationService()),
                new LocalizedMessages(messageSource));

        user = new User("alice", "encodedPassword");
        setId(user, 1L);
        when(authentication.getName()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(user);
    }

    @Test
    void keepsSearchConditionAndSortInModel() {
        final Diary diary = diary("感謝", "ありがとう", user);
        final Page<Diary> page = new PageImpl<>(List.of(diary));
        when(diaryRepository.findByUserAndUpdatedAtBetween(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        final ConcurrentModel model = new ConcurrentModel();
        assertThat(controller.index(0, "asc", "2026-08-27", model, authentication))
                .isEqualTo("index");
        assertThat(model.getAttribute("searchDate")).isEqualTo("2026-08-27");
        assertThat(model.getAttribute("searchActive")).isEqualTo(true);
        assertThat(model.getAttribute("sort")).isEqualTo("asc");
    }

    @Test
    void invalidDateReturnsEmptySearchResultInsteadOfAllDiaries() {
        final ConcurrentModel model = new ConcurrentModel();
        controller.index(0, "desc", "not-a-date", model, authentication);

        assertThat(model.getAttribute("invalidSearchDate")).isEqualTo(true);
        assertThat((List<?>) model.getAttribute("diaries")).isEmpty();
        verify(diaryRepository, never()).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    void pageLargerThanLastPageFallsBackToLastPage() {
        final Page<Diary> overflowPage = new PageImpl<>(
                List.of(),
                PageRequest.of(999_999, 7),
                21);

        final Diary diary = diary("最後の日記", "ありがとう", user);
        final Page<Diary> lastPage = new PageImpl<>(
                List.of(diary),
                PageRequest.of(2, 7),
                21);
        when(diaryRepository.findByUser(eq(user), any(Pageable.class)))
                .thenReturn(overflowPage)
                .thenReturn(lastPage);

        final ConcurrentModel model = new ConcurrentModel();
        controller.index(999_999, "desc", null, model, authentication);

        assertThat(model.getAttribute("currentPage")).isEqualTo(2);
        assertThat(model.getAttribute("totalPages")).isEqualTo(3);
        assertThat(model.getAttribute("diaries")).isEqualTo(List.of(diary));
        verify(diaryRepository, times(2)).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    void ownerCanUpdateDiary() throws Exception {
        final Diary diary = diary("今日", "ありがとう", user);
        setId(diary, 10L);
        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));

        final var response = controller.updateDiary(
                10L,
                "更新後",
                "今日もありがとうございます。",
                authentication);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("更新後");
        assertThat(diary.getTitle()).isEqualTo("更新後");
        assertThat(diary.getContent()).isEqualTo("今日もありがとうございます。");
        verify(diaryRepository).save(diary);
    }

    @Test
    void cannotUpdateAnotherUsersDiary() throws Exception {
        final User other = new User("bob", "encodedPassword");
        setId(other, 2L);
        final Diary diary = diary("他人の日記", "ありがとう", other);
        setId(diary, 11L);
        when(diaryRepository.findById(11L)).thenReturn(Optional.of(diary));

        assertThat(controller.updateDiary(
                11L,
                "更新後",
                "今日もありがとうございます。",
                authentication).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        verify(diaryRepository, never()).save(any(Diary.class));
    }

    @Test
    void cannotReadAnotherUsersDiary() throws Exception {
        final User other = new User("bob", "encodedPassword");
        setId(other, 2L);
        final Diary diary = diary("他人の日記", "ありがとう", other);
        setId(diary, 12L);
        when(diaryRepository.findById(12L)).thenReturn(Optional.of(diary));

        assertThat(controller.getDiary(12L, authentication).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Diary diary(final String title, final String content, final User owner) {
        final Diary diary = new Diary(title, content);
        diary.setUser(owner);
        return diary;
    }

    private void setId(final Object target, final Long id) throws Exception {
        final Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
