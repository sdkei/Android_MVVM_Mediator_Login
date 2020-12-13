package io.github.sdkei.loginmvvm.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** ログインの管理を行うリポジトリー。 */
@Suppress("ObjectPropertyName")
object LoginUseCase {

    /** ゲストユーザーのユーザー ID。 */
    const val GUEST_USER_ID = ""

    private val mutex = Mutex()

    private val userRepository: UserRepository
        get() = UserRepository

    /** ログイン中のユーザーのユーザー ID。 */
    val loginUserId: StateFlow<String?>
        get() = _loginUserId
    private val _loginUserId = MutableStateFlow<String?>(null)

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
                    _loginUserId.value = userId
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

            _loginUserId.value = GUEST_USER_ID
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

            _loginUserId.value = null
        }
    }

    /**
     * ログイン中であることを確認する。
     *
     * [mutex] でロックした状態で呼び出すこと。
     */
    private fun checkLogin() {
        check(mutex.isLocked)

        check(loginUserId.value != null) { "未だログインされていません。" }
    }

    /**
     * ログイン中でないことを確認する。
     *
     * [mutex] でロックした状態で呼び出すこと。
     */
    private fun checkNotLogin() {
        check(mutex.isLocked)

        check(loginUserId.value == null) { "既にログインされています。" }
    }
}