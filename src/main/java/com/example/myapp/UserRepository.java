package com.example.myapp;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ユーザーデータ（Userエンティティ）に対するデータベース操作を管理するリポジトリインターフェース。
 * Spring Data JPAにより、データベースへのCRUD処理や独自のクエリメソッドが自動生成されます。
 */
@Repository // 実務必須：コンポーネントの役割を明示し、SQL固有の例外をJava共通のデータアクセス例外に自動翻訳する機能を有効化
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 指定されたユーザー名（ログインID）をもとに、一意のユーザー情報をデータベースから取得します。
     * ログイン認証処理や、新規アカウント開設時のユーザー名重複チェックなどで利用されます。
     *
     * @param username 検索対象のユーザー名（一意である必要があります）
     * @return 該当ユーザー情報を含むOptionalオブジェクト。データが存在しない場合は Optional.empty() を返却。
     */
    Optional<User> findByUsername(final String username);
}