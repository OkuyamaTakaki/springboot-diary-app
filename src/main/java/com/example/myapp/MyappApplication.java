package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 日記アプリケーション（Myapp）のメイン起動クラス（エントリーポイント）。
 * 
 * `@SpringBootApplication` アノテーションにより、以下の3つの機能が自動で有効化されます：
 * 1. `@EnableAutoConfiguration`: クラスパス上のライブラリに応じた自動設定を有効化
 * 2. `@ComponentScan`: このパッケージおよび配下のコンポーネント（@Service, @Controller等）を自動検知
 * 3. `@SpringBootConfiguration`: このクラスが設定クラスの役割を兼ねることを明示
 */
@SpringBootApplication
public class MyappApplication {

    // 実務必須：アプリケーションの起動や終了をインフラ・保守運用チームが監視できるようSLF4Jロガーを定義
    private static final Logger log = LoggerFactory.getLogger(MyappApplication.class);

    /**
     * アプリケーションのメインメソッド（Javaプロセスの開始地点）。
     * 
     * @param args コマンドライン引数（本番環境での環境変数の上書きなどで利用）
     */
    public static void main(final String[] args) {
        log.info("日記アプリケーション（Myapp）の起動シーケンスを開始します。");

        try {
            // Spring Bootアプリケーションを実行し、コンテキスト（DIコンテナ）を立ち上げる
            SpringApplication.run(MyappApplication.class, args);
            
            log.info("日記アプリケーション（Myapp）が正常に起動しました。[ポート: 8080]");
            
        } catch (final Exception e) {
            // 実務必須：起動失敗（ポートの競合、DB接続エラーなど）した際に、原因を追跡できるようエラーログを記録
            log.error("日記アプリケーション（Myapp）の起動中に致命的なエラーが発生しました。", e);
            
            // 異常終了であることをオペレーティングシステム（OS）に明示的に通知してプロセスを終了
            System.exit(1);
        }
    }
}