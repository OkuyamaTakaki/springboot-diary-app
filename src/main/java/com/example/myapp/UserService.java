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
        log.debug("ユーザー情報をデータベースへ保存します。");

        final User savedUser = userRepository.save(user);

        log.debug("ユーザー情報の保存が完了しました。");
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
        log.debug("ユーザー名による検索を実行します。");
        return userRepository.findByUsername(username).orElse(null);
    }
}
