package com.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DiaryRepositoryTest {

    @Autowired
    private DiaryRepository diaryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldFindDiariesByUserAndDate() {
        final User user = new User("alice", "encodedPassword");
        entityManager.persist(user);

        final Diary matched = new Diary("朝の感謝", "今日はお店の人にありがとうと言えた");
        matched.setUser(user);
        matched.setCreatedAt(LocalDateTime.of(2026, 8, 15, 9, 0));
        matched.setUpdatedAt(LocalDateTime.of(2026, 8, 15, 9, 30));
        entityManager.persist(matched);

        final Diary other = new Diary("他の日の感謝", "別の日のこと");
        other.setUser(user);
        other.setCreatedAt(LocalDateTime.of(2026, 8, 16, 10, 0));
        other.setUpdatedAt(LocalDateTime.of(2026, 8, 16, 10, 30));
        entityManager.persist(other);

        entityManager.flush();

        final LocalDate targetDate = LocalDate.of(2026, 8, 15);
        final LocalDateTime start = targetDate.atStartOfDay();
        final LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        final List<Diary> result = diaryRepository.findByUserAndUpdatedAtBetween(user, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("朝の感謝");
    }
}
