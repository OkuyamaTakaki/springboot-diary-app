package com.example.myapp;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 日記データ（Diaryエンティティ）に対するデータベース操作を管理するリポジトリインターフェース。
 * Spring Data JPAにより、標準的なCRUD（作成・読込・更新・削除）処理が自動生成されます。
 */
@Repository // 実務必須：コンポーネントスキャンの対象であることを明示し、DB特有の例外翻訳（Exception Translation）を有効化
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    /**
     * 指定されたユーザーに紐付く日記データをすべて取得します。
     * 日記一覧画面などで、ログイン中ユーザー本人の過去データを抽出する際に利用します。
     *
     * @param user 日記の所有者（Userエンティティ）
     * @return 該当ユーザーが作成した日記データのリスト。データが存在しない場合は空のリストを返却。
     */
    List<Diary> findByUser(final User user);
}