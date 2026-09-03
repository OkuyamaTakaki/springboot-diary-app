# ありがとう日記

感謝を記録する、Spring Boot製の日記アプリです。利用者ごとにデータを分離し、自分の日記だけを登録・閲覧・編集できます。製品方針として削除機能は設けていません。

Codex担当の役割、触る範囲、統括への引渡し条件は [`AGENTS.md`](AGENTS.md) を参照してください。

## 現在の公開状態

現状は**ローカル公開候補**であり、製品完成・本番公開の承認済み状態ではありません。既存の31件の自動テスト、日英表示、主要な画面幅、入力失敗からの復帰、認証と所有者境界は確認済みです。一方、次の共通完成基準が未確認または不適合のため、公開判断は「停止」です。

- 80台の物理端末: Androidスマートフォン20台、iPhone 20台、AndroidタブレットまたはiPad 20台、Windows PC 20台
- 実Safari、Android WebView、物理タッチと実機キーボード
- 1920×1080、360×640、約200%文字拡大を含む日英の代表フロー
- 代表操作中の30 FPS維持と、人が実際に操作して確認する理解しやすさ・快適性・誤操作からの復帰
- 通常起動とRender休止復帰を含む、操作可能になるまで20秒以内の確認

不足証拠、公開責任者、公開先、復旧方法は [`Docs/RELEASE_DECISION_JA.md`](Docs/RELEASE_DECISION_JA.md) で管理します。AIの検査結果だけでは公開承認になりません。

## 主な機能

- ユーザー登録、ログイン、ログアウト
- 日記の新規登録と編集
- 新しい順・古い順の並べ替え
- 日付検索と検索条件を保つページ移動
- 検索0件と未投稿状態を区別した案内
- 否定文を除外する感謝表現チェック
- 日本語・英語の表示切替と選択言語の保存
- PC・スマートフォン対応のノート風デザイン
- 認証前から読める日本語・英語のプライバシー情報

## 構成

```text
src/main/java/       アプリケーション本体
src/main/resources/  画面、CSS、JavaScript、設定
src/test/            自動テスト
.github/             CodeQL、Dependabot、継続テスト
Docs/                ライセンス・引継ぎなど開発者向け資料
Dockerfile           Render用コンテナ設定
pom.xml              Java依存関係とビルド設定
変更点.md             主要な変更履歴
```

正本は、このREADMEと`pom.xml`があるリポジトリ直下です。`target/`は再生成できるビルド出力、`work/`はGit管理外のローカル運用領域です。古いコピーを見つけても、内容、Git履歴、参照先、復元用途を確認せずに削除・統合しないでください。PC固有の復元物一覧はGit公開対象外のローカル台帳で管理します。

## ローカルデータと機密情報の境界

`data/` と `work/` はGit除外済みで、現在のGit追跡ファイルは0件です。ただし物理的には正本内に残るため、分離は未完了です。内容を開かず確認した参照境界は次のとおりです。

- `data/`: ローカルH2 DB。`application.properties`の既定接続先から参照される。DB内容は検査・報告しない。
- `work/`: GitHub接続用ファイル、known-hosts、DPAPIファイル、Cookie設定、自動投稿用ローカルスクリプトを含む。秘密値や資格情報は検査・報告しない。
- `.git/config`: `work/`内のSSH鍵とknown-hostsを参照するローカル設定。
- Render／Neon: 接続情報は環境変数で受け取る設計だが、実際の地域、プラン、費用、保存期間、復旧条件は未確認。

分離先候補は`%LOCALAPPDATA%\OkuyamaTakaki\ArigatouDiary`です。現在は計画記録までで、コピーや参照切替は未実施です。復元可能な段階手順は [`Docs/LOCAL_DATA_SEPARATION.md`](Docs/LOCAL_DATA_SEPARATION.md) を参照してください。移動、ACL変更、参照先変更は保護操作として、正確な対象と復旧手順を提示して人の確認を得てから行います。本日分の日記はこの検査では投稿していません。既存DBと本番DBを読んでいないため既存投稿の有無は未確認で、自動投稿も未承認です。

自動投稿の確定条件、未確定条件、安全な停止・再開境界は[`Docs/AUTOMATIC_POSTING_PLAN.md`](Docs/AUTOMATIC_POSTING_PLAN.md)に集約しています。期間と時間帯が決まっていても、本文、対象アカウント、削除対象が確定するまでは外部投稿や既存データ削除を行いません。

## 久しぶりに作業を再開するとき

1. このREADMEと `変更点.md` を読む。
2. `git status` で未保存の変更を確認し、他の作業を上書きしない。
3. Java 21を確認し、`./mvnw verify` を実行する。
4. 通常の画面変更は `src/main/resources/templates/` と `src/main/resources/static/` から確認する。
5. 外部ライブラリ・素材を変更した場合は [`Docs/THIRD_PARTY_NOTICES.md`](Docs/THIRD_PARTY_NOTICES.md) を更新する。
6. 最終確認の実績と再現手順は [`Docs/VERIFICATION_2026-09-01.md`](Docs/VERIFICATION_2026-09-01.md) を確認する。
7. 公開前は [`Docs/PRIVACY_RELEASE_CHECKLIST.md`](Docs/PRIVACY_RELEASE_CHECKLIST.md) の未確定項目を解消する。
8. [`Docs/RELEASE_DECISION_JA.md`](Docs/RELEASE_DECISION_JA.md) へ、権利、データ、費用、実機、復元方法、人の判断を記録する。

生成物やローカルDBをソースと混同せず、復元できるバックアップを確認してから移動・削除してください。

### CodexやPCが途中終了した場合

1. ビルドや投稿をすぐ再開せず、Java、Maven、Python、Git、SSHの関連プロセスと待受ポートを確認する。
2. `git status --short --untracked-files=all`を実行し、[`Docs/RELEASE_DECISION_JA.md`](Docs/RELEASE_DECISION_JA.md) の開始時復帰点と比較する。
3. 既存のJARとテスト記録が対象ソースに対応するか確認し、不明な場合だけ負荷予算を計測して再検証する。
4. `data/`、`work/`、Git設定、外部サービスを移動・削除・変更せず、最後に完了した安全な工程から再開する。
5. この文書更新だけを戻す場合も、一括resetやcheckoutを使わず、該当段落だけをレビューして戻す。

## ローカル実行

Java 21を用意し、プロジェクト直下で次を実行します。

```shell
./mvnw spring-boot:run
```

ローカルではファイル型H2を使用します。Web上のH2管理画面は安全のため依存関係ごと除外しています。

## テスト

```shell
./mvnw verify
```

RenderのDockerビルドでも同じテストを必ず実行します。GitHubでは通常テスト、CodeQL、Dependabotを継続実行します。

2026-09-02にJava 21.0.12とMaven Wrapperで最新版をオフライン・1スレッド検証し、`verify`、31件のテスト、実行JAR生成がすべて成功しました。失敗・エラー・スキップは0件です。詳しい実績と再実行方法は [`Docs/VERIFICATION_2026-09-01.md`](Docs/VERIFICATION_2026-09-01.md) を参照してください。

## 対応画面と操作性

- PCでは一覧表、960px以下ではカード表示へ切り替え、縦横の画面変更に追従します。
- 主要な入力・ボタン・言語切替は44px以上の操作領域を確保し、キーボードのフォーカス位置を見えるようにしています。
- `viewport-fit=cover` とsafe-area余白を使い、ノッチやホームインジケーターとの重なりを避けます。
- 日本語・英語の長さの違いと長いユーザー名による横はみ出しを抑制します。
- Chromiumの画面幅シミュレーションでAndroidスマートフォン、iPhone、iPad、Windows PC相当の縦横表示を確認済みです。これはローカル公開候補の確認であり、実機のSafari、Android WebView、物理タッチ操作、200%文字拡大、継続30 FPS、80台以上の実機確認は公開前の残確認です。

## 表示言語

ログイン画面、登録画面、日記画面、通知、入力エラー、エラー画面を日本語・英語で表示できます。各画面の `日本語 / English` から切り替え、選択は個人情報を含まない `diary-language` Cookieへ1年間保存します。初回と不明な言語は日本語へ戻ります。

画面文字列は `src/main/resources/messages.properties` と `messages_en.properties` が正本です。キーの不足、余分、空文字は `LocalizationResourceTest` で検出します。翻訳サービスへの送信や実行時の外部通信はありません。

## 本番設定

本番では次の値をRenderの秘密環境変数へ設定します。実値やパスワードはソースへ保存しません。

- `DATABASE_URL`: ユーザー名・パスワードを含めないNeonのJDBC接続先
- `SPRING_DATASOURCE_USERNAME`: NeonのDBユーザー名
- `SPRING_DATASOURCE_PASSWORD`: NeonのDBパスワード
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`: `org.postgresql.Driver`
- `SPRING_PROFILES_ACTIVE`: `prod`
- `APP_PRIVACY_CONTACT_URL`: 仮GitHubプロフィール導線を正式なHTTPSまたはmailtoの問い合わせ先で上書きする設定

Render再起動後もデータが残るよう、無料のNeon PostgreSQLを使用します。無料枠には容量・稼働時間・復元期間の制限があり、無停止運用や永久保管を保証する有料SLAはありません。重要なデータはiCloudなどへ定期的に別バックアップしてください。

本番では未使用のDB接続を起動時に増やさず、必要になった分だけ接続します。Renderの休止復帰、JVM、DB接続、Spring Boot初期化をログから区別する方法は [`Docs/STARTUP_DIAGNOSTICS.md`](Docs/STARTUP_DIAGNOSTICS.md) を参照してください。

無料環境が休止していると、最初のアクセスでRenderの `Application loading` が約1分表示される場合があります。ログイン・新規登録画面の日英案内に従い、1〜2分待ってから一度だけ再読み込みしてください。連続再読み込みや異常URLの連続検証は避けます。この待機画面はアプリ起動前にRenderが表示するため、アプリ内のCSSでは変更できません。

約1分の休止復帰は、共通完成基準の「20秒以内に操作可能」を満たしません。20秒以内の実測証拠またはホスティング方針の見直しが得られるまでは、製品完成や公開可能とは判定しません。

## セキュリティ方針

- パスワードはBCryptでハッシュ化
- CSRF、認証、所有者照合で不正な登録・閲覧・編集を遮断
- ThymeleafのエスケープとCSPでXSSを多層防御
- HSTS、クリックジャッキング防止、権限制限ヘッダーを付与
- DB接続情報はRenderの秘密設定だけで管理
- DBパスワードは接続先と分離し、起動ログへ出力しない
- 実行コンテナは非管理者ユーザーで起動

一般ユーザーがDBやSQLを直接操作する機能はありません。アプリを通じて自分の日記だけを扱えます。

## プライバシー情報

`/privacy` は認証前から開け、ログイン、登録、日記の各画面から移動できます。保存項目、Cookie、RenderとNeonの利用、第三者提供、保存期間、安全管理を日本語と英語で説明します。

問い合わせ先は仮に公開済みGitHubプロフィールを案内し、非公開フォームではないことと秘密情報・個人情報を送らないことを日英で明示します。正式窓口は個人の連絡先をソースへ埋め込まず、`APP_PRIVACY_CONTACT_URL`で上書きします。正式窓口、保存地域、保存期間、削除依頼の手順が確定するまでは本番へ公開しません。判断と確認手順は [`Docs/PRIVACY_RELEASE_CHECKLIST.md`](Docs/PRIVACY_RELEASE_CHECKLIST.md) に記録しています。

## 外部ライブラリ・素材

主要な依存関係、Webフォント、公式ライセンス確認先、公開前の再確認手順は [`Docs/THIRD_PARTY_NOTICES.md`](Docs/THIRD_PARTY_NOTICES.md) に記録しています。

画面フォントはSIL Open Font License 1.1のRounded M+ 1cを `src/main/resources/static/fonts/` から自己配信します。許諾文は `Docs/Licenses/RoundedMplus1c/OFL.txt` に保存し、閲覧時にGoogle Fontsへ接続しません。
