# 外部ライブラリ・素材の確認記録

## 結論と確認範囲

この記録は、`pom.xml`、ローカルMavenメタデータ、2026-09-01の実行可能JAR、同梱フォントと許諾文から確認できた主要要素を、人が公開前に追跡できる形へ整理したものです。取得日は過去の記録がないため断定せず、最終確認日だけを記載します。これは法的助言や配布可否の保証ではありません。

## 主要な外部要素

| 対象・確認版 | 提供元 | ライセンス | 商用・変更・再配布の確認要点 | 用途とローカル根拠 |
|---|---|---|---|---|
| Spring Boot / starters 4.1.1 | Springプロジェクト | Apache License 2.0 | 商用利用・変更・再配布可。ライセンス、著作権、該当するNOTICEを保持 | アプリ基盤。`pom.xml` とローカルの親POM |
| Spring Framework 7.0.9 / Spring Security 7.1.1 | Springプロジェクト | Apache License 2.0 | 同上 | Web、DI、認証・認可。実行可能JAR内の依存一覧 |
| Thymeleaf 3.1.5.RELEASE | Thymeleaf Team | Apache License 2.0 | 同上。公開前に版付属のNOTICEも再確認 | HTMLテンプレート。実行可能JAR内の依存一覧 |
| Apache Tomcat 11.0.24 | Apache Software Foundation | Apache License 2.0 | 同上 | 組み込みWebサーバー。ローカルPOMと実行可能JAR |
| Hibernate ORM 7.4.5.Final | Hibernate.org | Apache License 2.0 | 同上 | JPA実装。ローカルPOMと実行可能JAR |
| H2 Database 2.4.240 | Thomas Mueller / H2 contributors | MPL 2.0 または EPL 1.0 | デュアルライセンス。選択した条件と変更ファイルの義務を保持 | ローカル開発用DB。`pom.xml` とローカルPOM |
| PostgreSQL JDBC 42.7.13 | PostgreSQL Global Development Group | BSD 2-Clause | 商用利用・変更・再配布可。著作権表示と条件を保持 | 本番DB接続。`pom.xml` とローカルPOM |
| Jackson 3.1.5 / annotations 2.21 | FasterXML contributors | Apache License 2.0 | 商用利用・変更・再配布可。版付属のNOTICEを再確認 | JSON等の推移依存。実行可能JAR内の依存一覧 |
| JUnit Jupiter 6.0.3 | JUnit Team | EPL 2.0 | 商用利用・変更・再配布可。配布時はライセンスと該当義務を保持 | テスト専用。Spring Boot依存管理メタデータ |
| Mockito 5.23.0 | Mockito contributors | MIT License | 商用利用・変更・再配布可。著作権表示と許諾文を保持 | テスト専用Javaエージェント。依存管理メタデータ |
| AssertJ 3.27.7 | AssertJ contributors | Apache License 2.0 | 商用利用・変更・再配布可。ライセンス等を保持 | テスト専用。ローカルPOM |
| Maven Surefire / Spring Boot Maven Plugin | Apache / Springプロジェクト | Apache License 2.0 | ビルド時のみ。再配布時は該当ライセンスを保持 | テスト・パッケージ作成。`pom.xml` |
| Rounded M+ 1c Regular | M+ FONTS PROJECT / Google Fonts配布 | SIL Open Font License 1.1 | 商用利用・変更・再配布可。OFL本文を保持し、フォント単体販売をしない | 画面フォント。`static/fonts` と `Docs/Licenses/RoundedMplus1c/OFL.txt` |

## 公開前の確認手順

1. `pom.xml` と `./mvnw dependency:tree` で、実際に使用する直接・推移依存関係を確認する。
2. 配布するコンテナや成果物に含まれるライセンス表示義務を、各公式ライセンス本文で確認する。
3. 自己配信フォント、OFL本文、CSPの`font-src 'self'`、未認証画面での`/fonts/**`許可をまとめて確認する。
4. 画像・動画・文章を追加した場合は、作者、入手元、ライセンス、取得日、加工内容をこの文書へ追記する。
5. 秘密情報、個人情報、日記本文、認証情報がリポジトリや成果物へ混入していないことを確認する。
6. 実行可能JARまたはコンテナから推移依存一覧を生成し、各版の `LICENSE`・`NOTICE` と本表の差分を人が確認する。

## 現時点の注意点

- 2026-09-01の実行可能JARには78個の実行時ライブラリが含まれます。推移依存すべてのライセンス本文・NOTICEをこの文書へ複製してはいないため、一般公開物を更新する直前に完全な依存ライセンス一覧を再生成してください。
- 外部要素の当初取得日は記録から特定できませんでした。今後追加する素材は取得日、入手元、作者、加工内容も同時に記録してください。
- Mavenテスト専用依存は通常の実行可能JARへ含めない前提です。パッケージ内容を変えた場合は再確認してください。
- `RoundedMplus1c-Regular.ttf` だけを同梱し、500・700の表示はブラウザの合成ウェイトです。将来ウェイト別ファイルを追加する場合も、公式配布元とOFL本文を維持します。
- フォントSHA-256: `B75708B53E45B06D17D470AEECA5B766E3D1B3999F03F13EC4EB863CA846C14C`
- OFL本文SHA-256: `67F64C5509E5151796599E3AD47C3131CBE0C80C4F9430B90236A1249C2EACC9`
- Google Fontsの外部CSS、preconnect、CSP許可は2026-09-01に除去済みです。

最終確認日: 2026-09-01
