package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        diaryRepository = org.mockito.Mockito.mock(DiaryRepository.class);
        userService = org.mockito.Mockito.mock(UserService.class);
        authentication = org.mockito.Mockito.mock(Authentication.class);
        controller = new DiaryController(
                diaryRepository,
                userService,
                new DiaryValidationService(new InputValidationService()));

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
    void ownerCanDeleteDiary() throws Exception {
        final Diary diary = diary("今日", "ありがとう", user);
        setId(diary, 10L);
        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));

        assertThat(controller.deleteDiary(10L, authentication).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        verify(diaryRepository).delete(diary);
    }

    @Test
    void cannotDeleteAnotherUsersDiary() throws Exception {
        final User other = new User("bob", "encodedPassword");
        setId(other, 2L);
        final Diary diary = diary("他人の日記", "ありがとう", other);
        setId(diary, 11L);
        when(diaryRepository.findById(11L)).thenReturn(Optional.of(diary));

        assertThat(controller.deleteDiary(11L, authentication).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        verify(diaryRepository, never()).delete(any(Diary.class));
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
