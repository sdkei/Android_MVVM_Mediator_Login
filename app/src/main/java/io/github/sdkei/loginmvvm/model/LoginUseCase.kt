package io.github.sdkei.loginmvvm.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/** ユーザー登録の管理とログインしているユーザーの管理を行うリポジトリー。 */
@Suppress("ObjectPropertyName")
object LoginUseCase {

    /** ゲストユーザーのユーザー ID。 */
    const val GUEST_USER_ID = ""

    private val userRepository: UserRepository
        get() = UserRepository

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
     * 登録ユーザーでログインする。
     *
     * @return 成功したかどうか。
     */
    suspend fun loginRegisteredUser(userId: String, password: String): Boolean {
        yield()

        checkNotLogin()

        val isSucceeded =
            userRepository.authenticate(userId, password)

        if (isSucceeded.not()) {
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

    /** ログイン中であることを確認する。 */
    private fun checkLogin() {
        check(isUserLoggingIn) { "未だログインされていません。" }
    }

    /** ログイン中でないことを確認する。 */
    private fun checkNotLogin() {
        check(isUserLoggingIn.not()) { "既にログインされています。" }
    }
}