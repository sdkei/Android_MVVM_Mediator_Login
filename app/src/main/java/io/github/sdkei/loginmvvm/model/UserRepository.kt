package io.github.sdkei.loginmvvm.model

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 登録ユーザーを管理するリポジトリー。 */
object UserRepository {

    private val mutex = Mutex()

    /** ユーザー名簿。 */
    // 簡単のために Map にする。本来はローカル DB やリモート API などとなる。
    private val users: MutableMap<String, String> = mutableMapOf()

    init {
        // ユーザーを一人登録しておく。（ユーザー追加 UI を省略するため。）
        runBlocking {
            register("user", "password")
        }
    }

    /**
     * ユーザーを登録する。
     *
     * @param userId ユーザー ID。空は不可。
     * @param password パスワード。空は不可。
     *
     * @return 成功したかどうか。
     *  同じユーザーIDのアカウントがすでに存在する場合に限り失敗する。
     */
    suspend fun register(userId: String, password: String): Boolean {
        requireUserIdAndPassword(userId, password)

        mutex.withLock {
            users[userId]?.also {
                return false
            }

            users[userId] = password

            return true
        }
    }

    /**
     * 登録ユーザーを認証する。
     *
     * @return 成功したかどうか。
     */
    suspend fun authenticate(userId: String, password: String): Boolean {
        requireUserIdAndPassword(userId, password)

        mutex.withLock {
            return users[userId] == password
        }
    }

    /** 引数で与えられたユーザー ID とパスワードが適切なフォーマットであるかどうかを確認する。 */
    private fun requireUserIdAndPassword(userId: String, password: String) {
        require(userId.isNotEmpty()) { "ユーザー ID が空です。" }
        require(password.isNotEmpty()) { "パスワードが空です。" }
    }
}