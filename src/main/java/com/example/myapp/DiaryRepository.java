package com.example.myapp;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 日記データへのアクセスを担当するリポジトリ。
 */
@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    /**
     * 指定したユーザーの日記をページングして取得する。
     *
     * @param user ユーザー
     * @param pageable ページングおよびソート条件
     * @return 指定したユーザーの日記
     */
    Page<Diary> findByUser(User user, Pageable pageable);

    /**
     * 指定した日付の範囲に含まれる日記を取得する。
     *
     * @param user 検索対象ユーザー
     * @param start 開始日時
     * @param end 終了日時
     * @return 対象の日記一覧
     */
    List<Diary> findByUserAndUpdatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    /**
     * 指定した日付の範囲に含まれる日記をページングして取得する。
     *
     * @param user 検索対象ユーザー
     * @param start 開始日時
     * @param end 終了日時
     * @param pageable ページングおよびソート条件
     * @return 対象の日記一覧
     */
    Page<Diary> findByUserAndUpdatedAtBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
