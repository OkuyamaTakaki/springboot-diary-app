package com.example.myapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー情報の登録・検索を担当するサービス。
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    /**
     * コンストラクタによる依存性注入。
     */
    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザーをデータベースへ登録します。
     *
     * @param user 登録するユーザー
     * @return 保存されたユーザー
     */
    @Transactional
    public User saveUser(final User user) {
        log.info("ユーザー登録処理を開始します。ユーザー名: {}", user.getUsername());

        final User savedUser = userRepository.save(user);

        log.info("ユーザー登録が完了しました。ユーザー名: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * ユーザー名からユーザー情報を検索します。
     *
     * @param username 検索するユーザー名
     * @return 該当ユーザー。存在しない場合はnull
     */
    @Transactional(readOnly = true)
    public User findByUsername(final String username) {
        log.debug("ユーザー検索を実行します。ユーザー名: {}", username);
        return userRepository.findByUsername(username).orElse(null);
    }
}