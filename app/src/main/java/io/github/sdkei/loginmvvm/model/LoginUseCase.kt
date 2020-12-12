package io.github.sdkei.loginmvvm.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** ユーザー登録の管理とログインしているユーザーの管理を行うリポジトリー。 */
@Suppress("ObjectPropertyName")
object LoginUseCase {

    /** ゲストユーザーのユーザー ID。 */
    const val GUEST_USER_ID = ""

    private val mutex = Mutex()

    private val userRepository: UserRepository
        get() = UserRepository

    /** ログイン中のユーザーのユーザー ID。 */
    private var loginUserId: String? = null

    /** ログイン中のユーザーのユーザー ID。 */
    val loginUserIdFlow: StateFlow<String?>
        get() = _loginUserIdFlow
    private val _loginUserIdFlow = MutableStateFlow(loginUserId)

    /**
     * ログイン中のユーザーのユーザー ID を返す。
     *
     * [mutex] でロックした状態で呼び出さ **ない** こと。
     */
    suspend fun getLoginUserId(): String? =
        mutex.withLock {
            loginUserId
        }

    /**
     * ログイン中のユーザーのユーザー ID をセットする。
     *
     * [mutex] でロックした状態で呼び出すこと。
     */
    private suspend fun setLoginUser(value: String?) {
        check(mutex.isLocked)

        if (loginUserId == value) return

        loginUserId = value

        _loginUserIdFlow.emit(value)
    }

    /**
     * 登録ユーザーでログインする。
     *
     * [mutex] でロックした状態で呼び出さ **ない** こと。
     *
     * @return 成功したかどうか。
     */
    suspend fun loginRegisteredUser(userId: String, password: String): Boolean {
        mutex.withLock {
            checkNotLogin()

            return userRepository.authenticate(userId, password).also { isSucceeded ->
                if (isSucceeded) {
                    loginUserId = userId
                }
            }
        }
    }

    /**
     * ゲストユーザーでログインする。
     *
     * [mutex] でロックした状態で呼び出さ **ない** こと。
     */
    suspend fun loginGuest() {
        mutex.withLock {
            checkNotLogin()

            loginUserId = GUEST_USER_ID
        }
    }

    /**
     * ログアウトする。
     *
     * [mutex] でロックした状態で呼び出さ **ない** こと。
     */
    suspend fun logout() {
        mutex.withLock {
            checkLogin()

            loginUserId = null
        }
    }

    /**
     * ログイン中であることを確認する。
     *
     * [mutex] でロックした状態で呼び出すこと。
     */
    private fun checkLogin() {
        check(mutex.isLocked)

        check(loginUserId != null) { "未だログインされていません。" }
    }

    /**
     * ログイン中でないことを確認する。
     *
     * [mutex] でロックした状態で呼び出すこと。
     */
    private fun checkNotLogin() {
        check(mutex.isLocked)

        check(loginUserId == null) { "既にログインされています。" }
    }
}