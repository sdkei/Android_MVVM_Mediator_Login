package io.github.sdkei.loginmvvm.model

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** ユーザー登録の管理とログインしているユーザーの管理を行うリポジトリー。 */
@Suppress("ObjectPropertyName")
object LoginRepository {

    /** ユーザー名簿。 */
    // 簡単のために Map にする。排他制御も省略する。本来はローカル DB やリモート API などとなる。
    private val users: MutableMap<String, String> = mutableMapOf()

    init {
        // ユーザーを一人登録しておく。（ユーザー追加 UI を省略するため。）
        runBlocking {
            registerUser("user", "password")
        }
    }

    /** ゲストユーザーのユーザー ID。 */
    const val GUEST_USER_ID = ""

    /** ログイン中のユーザーのユーザー ID。 */
    var loginUserId: String? = null
        private set(value) {
            if (field == value) return

            field = value
            GlobalScope.launch(Dispatchers.Default) {
                _loginUserIdFlow.emit(value)
            }
        }

    val isUserLoggingIn: Boolean
        get() = loginUserId != null

    val loginUserIdFlow: StateFlow<String?>
        get() = _loginUserIdFlow
    private val _loginUserIdFlow = MutableStateFlow(loginUserId)

    /**
     * ユーザーを登録する。
     *
     * @return 成功したかどうか。
     *  同じユーザーIDのアカウントがすでに存在する場合に限り失敗する。
     */
    suspend fun registerUser(userId: String, password: String): Boolean {
        requireUserIdAndPassword(userId, password)

        yield()

        users[userId]?.also {
            return false
        }

        users[userId] = password

        return true
    }

    /**
     * 登録ユーザーでログインする。
     *
     * @return 成功したかどうか。
     */
    suspend fun loginRegisteredUser(userId: String, password: String): Boolean {
        requireUserIdAndPassword(userId, password)

        yield()

        checkNotLogin()

        if (users[userId] != password) {
            return false
        }

        loginUserId = userId

        return true
    }

    /** ゲストユーザーでログインする。 */
    suspend fun loginGuest() {
        yield()

        checkNotLogin()

        loginUserId = GUEST_USER_ID
    }

    /** ログアウトする。 */
    suspend fun logout() {
        checkLogin()

        yield()

        loginUserId = null
    }

    /** 引数で与えられたユーザー ID とパスワードが適切なフォーマットであるかどうかを確認する。 */
    private fun requireUserIdAndPassword(userId: String, password: String) {
        require(userId.isNotEmpty()) { "ユーザー ID が空です。" }
        require(password.isNotEmpty()) { "パスワードが空です。" }
    }

    /** ログイン中であることを家訓する。 */
    private fun checkLogin() {
        check(isUserLoggingIn) { "未だログインされていません。" }
    }

    /** ログイン中でないことを確認する。 */
    private fun checkNotLogin() {
        check(isUserLoggingIn.not()) { "既にログインされています。" }
    }
}