package com.example.myapp;

// ★最重要：この2行が絶対に必要です
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー情報の登録、検索に関わるビジネスロジックを提供するサービス。
 * コントローラー層からの要求を受け、データの整合性を保ちながらデータベースと連携します。
 */
@Service
public class UserService {

    // 実務必須：System.out.printlnを廃止し、本番環境の運用監視システムに連動するSLF4Jロガーを採用
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * コンストラクタによる安全な依存性注入（DI）。
     */
    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 新規ユーザーをデータベースに登録（保存）します。
     * 
     * @param user 登録対象のユーザーエンティティ
     * @return 永続化（保存）が完了したユーザーエンティティ
     */
    @Transactional // 実務必須：データの書き込み処理におけるトランザクション（整合性）を保証
    public User saveUser(final User user) {
        log.info("データベースへのユーザー登録処理を開始します。ユーザー名: {}", user.getUsername());

        // データベースへ保存
        final User savedUser = userRepository.save(user);

        log.info("ユーザーの登録が正常に完了しました。登録ユーザー名: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * 指定されたユーザー名をもとに、データベースからユーザー情報を検索します。
     * 
     * @param username 検索対象のユーザー名（ログインID）
     * @return 該当するユーザー情報。存在しない場合は null を返却します。
     */
    @Transactional(readOnly = true) // 実務必須：読み取り専用トランザクションにすることで、DB負荷を軽減しパフォーマンスを最適化
    public User findByUsername(final String username) {
        log.debug("ユーザー名による検索を実行します。検索キー: {}", username);
        
        // リポジトリが返すOptionalを安全にハンドリング（データがなければnullを返す仕様を継承）
        return userRepository.findByUsername(username).orElse(null);
    }
}