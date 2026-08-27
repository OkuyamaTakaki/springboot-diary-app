package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 日記アプリケーションのエントリーポイント。 */
@SpringBootApplication
public class MyappApplication {

    private static final Logger log = LoggerFactory.getLogger(MyappApplication.class);

    /**
     * @param args コマンドライン引数
     */
    public static void main(final String[] args) {
        log.info("ありがとう日記を起動します。");
        SpringApplication.run(MyappApplication.class, args);
    }
}
