# ありがとう日記

感謝を記録する、Spring Boot製の日記アプリです。利用者ごとにデータを分離し、自分の日記だけを登録・閲覧・編集できます。製品方針として削除機能は設けていません。

## 主な機能

- ユーザー登録、ログイン、ログアウト
- 日記の新規登録と編集
- 新しい順・古い順の並べ替え
- 日付検索と検索条件を保つページ移動
- 検索0件と未投稿状態を区別した案内
- 否定文を除外する感謝表現チェック
- PC・スマートフォン対応のノート風デザイン

## 構成

```text
src/main/java/       アプリケーション本体
src/main/resources/  画面、CSS、JavaScript、設定
src/test/            自動テスト
.github/             CodeQL、Dependabot、継続テスト
Dockerfile           Render用コンテナ設定
pom.xml              Java依存関係とビルド設定
変更点.md             主要な変更履歴
```

## ローカル実行

Java 21を用意し、プロジェクト直下で次を実行します。

```shell
./mvnw spring-boot:run
```

ローカルではファイル型H2を使用します。Web上のH2管理画面は安全のため依存関係ごと除外しています。

## テスト

```shell
./mvnw clean verify
```

RenderのDockerビルドでも同じテストを必ず実行します。GitHubでは通常テスト、CodeQL、Dependabotを継続実行します。

## 本番設定

本番では次の値をRenderの秘密環境変数へ設定します。実値やパスワードはソースへ保存しません。

- `DATABASE_URL`: ユーザー名・パスワードを含めないNeonのJDBC接続先
- `SPRING_DATASOURCE_USERNAME`: NeonのDBユーザー名
- `SPRING_DATASOURCE_PASSWORD`: NeonのDBパスワード
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`: `org.postgresql.Driver`
- `SPRING_PROFILES_ACTIVE`: `prod`

Render再起動後もデータが残るよう、無料のNeon PostgreSQLを使用します。無料枠には容量・稼働時間・復元期間の制限があり、無停止運用や永久保管を保証する有料SLAはありません。重要なデータはiCloudなどへ定期的に別バックアップしてください。

## セキュリティ方針

- パスワードはBCryptでハッシュ化
- CSRF、認証、所有者照合で不正な登録・閲覧・編集を遮断
- ThymeleafのエスケープとCSPでXSSを多層防御
- HSTS、クリックジャッキング防止、権限制限ヘッダーを付与
- DB接続情報はRenderの秘密設定だけで管理
- DBパスワードは接続先と分離し、起動ログへ出力しない
- 実行コンテナは非管理者ユーザーで起動

一般ユーザーがDBやSQLを直接操作する機能はありません。アプリを通じて自分の日記だけを扱えます。
