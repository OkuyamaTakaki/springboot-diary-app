package com.example.myapp;

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
     * 指定したユーザーの日記をページングして取得します。
     * ページ番号や1ページあたりの件数はPageableで指定します。
     *
     * @param user 日記の所有者
     * @param pageable ページ番号・表示件数などのページング条件
     * @return 指定ユーザーの日記をページングした結果
     */
    Page<Diary> findByUser(final User user, final Pageable pageable);
}