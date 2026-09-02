# Render起動遅延の診断（2026-09-01）

## 結論

公開URLで見えた約50秒超の `Application loading` だけでは、Renderの休止復帰、コンテナ起動、Neonへの接続、Spring Boot初期化のどこに時間を使ったか断定できません。正本には、起動時の外部HTTP通信、初期データ投入、明示的な待機処理はありません。支払・プラン・秘密設定を変えずにできる改善として、本番の未使用DB接続を事前生成しない設定を追加しました。

## アプリ側で確認できた要因

- 実行JARは約61MBで、Spring Boot、Tomcat、Security、Thymeleaf、JPA、PostgreSQL JDBCを含む。
- `/health` はDBを読まずに応答するため、起動完了後の監視処理は軽い。
- JPAは起動時にDBへ接続し、`spring.jpa.hibernate.ddl-auto=update` でスキーマを確認する。この処理はDBの休止復帰や通信待ちの影響を受ける。
- HikariCPは既定でアイドル接続を保持する。`prod`だけ `minimum-idle=0` とし、Hibernateが必要とする接続以外を起動時に先回りして増やさない。
- H2はローカル開発用、PostgreSQLは本番用として同じ成果物に含まれる。H2除去だけで50秒単位の改善になる根拠はなく、ローカル復旧性を優先して現状維持とした。

## Renderログでの切り分け

秘密値を表示せず、次の時刻だけを比較します。

1. Renderが起動開始を示した時刻
2. `Starting MyappApplication` が出た時刻
3. `HikariPool-... - Starting` と `Start completed` の時刻
4. `Tomcat started` と `Started MyappApplication in ... seconds` の時刻
5. 最初の `/health` HTTP 200の時刻

- 1から2が長い: Render側の休止復帰、イメージ準備、JVM開始前の領域。
- 2から3、またはHikari開始から完了が長い: DB接続・Neon休止復帰・ネットワークの可能性。
- DB接続後から4が長い: Hibernateスキーマ確認やSpring初期化の可能性。
- 4の直後に5が返る: アプリのヘルスチェック経路は正常。

ログを共有するときは、JDBC URL、DBユーザー名、パスワード、Cookie、日記本文を含めません。

## 今回変更しなかった項目

- `ddl-auto=update` を `validate` や `none` へ変えること: 現在の本番スキーマを確認せずに変えると、初回構築や将来の更新が失敗するため。
- DB接続タイムアウトを短縮すること: Neonの実プランと通常の休止復帰時間が未確認で、正常起動まで失敗させるおそれがあるため。
- JVMを起動優先モードへ固定すること: 実環境のCPU・メモリ・負荷を測らずに設定すると、起動後の処理性能を下げ得るため。
- Render／Neonのプラン変更、常時監視、外部ping: 支払・規約・外部設定の判断が必要なため。

## 公開後の判断材料

少なくとも3回、休止後の起動について上記5時刻を記録します。アプリ内部の `Started ... in` が短いのに全体だけ約50秒なら、コード変更で除けないRender側の休止復帰が主因です。HikariやHibernateが長い場合だけ、実プラン、保存地域、接続方式、スキーマ移行方針を確認して次の改善を判断します。
