package com.example.myapp;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 日記データへのアクセスを担当するリポジトリ。
 */
@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    /**
     * 指定したユーザーの日記を取得します。
     *
     * @param user 日記の所有者
     * @return 該当ユーザーの日記一覧
     */
    List<Diary> findByUser(final User user);
}