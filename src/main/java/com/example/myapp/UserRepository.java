package com.example.myapp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ユーザーデータへのデータベースアクセスを担当するリポジトリ。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ユーザー名からユーザー情報を取得します。
     *
     * @param username 検索するユーザー名
     * @return 該当ユーザー。存在しない場合はOptional.empty()
     */
    Optional<User> findByUsername(final String username);
}