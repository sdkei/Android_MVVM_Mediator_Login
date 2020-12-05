package io.github.sdkei.loginmvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sdkei.loginmvvm.model.LoginRepository
import io.github.sdkei.loginmvvm.model.UserType
import io.github.sdkei.loginmvvm.utils.CloseableObservingManager
import io.github.sdkei.loginmvvm.utils.exhaustive
import io.github.sdkei.loginmvvm.utils.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** ログイン画面の [ViewModel]。 */
class LoginViewModel : ViewModel() {

    /** メッセージを送出する [Flow]。 */
    val message: SharedFlow<Message>
        get() = _message
    private val _message = MutableSharedFlow<Message>()

    /** ゲスト／登録済みユーザーのどちらが選択されているか。 */
    val userType = MutableLiveData<UserType>(UserType.GUEST)

    /** ユーザー ID 入力欄の値。 */
    val userId = MutableLiveData<String>("")

    /** ユーザー ID 入力欄を有効にするかどうか。 */
    val isUserIdEnabled: LiveData<Boolean>
        get() = _isUserIdEnabled
    private val _isUserIdEnabled = MutableLiveData(false)

    /** パスワード入力欄の値。 */
    val password = MutableLiveData<String>("")

    /** パスワード入力欄を有効にするかどうか。 */
    val isPasswordEnabled: LiveData<Boolean>
        get() = _isPasswordEnabled
    private val _isPasswordEnabled = MutableLiveData(false)

    /** OK ボタンを有効にするかどうか。 */
    val isOkEnabled: LiveData<Boolean>
        get() = _okEnabled
    private val _okEnabled = MutableLiveData(true)

    /** キャンセルボタンを有効にするかどうか。 */
    val isCancelEnabled: LiveData<Boolean> = MutableLiveData(true) // 常に有効にする。

    private val loginRepository = LoginRepository

    private val observingManager = CloseableObservingManager()

    override fun onCleared() {
        observingManager.close()
    }

    // プロパティが更新されたときに、それに依存する他のプロパティが更新されるように監視を開始する。
    init {
        userType.observe(observingManager) {
            updateStatus()
        }
        userId.observe(observingManager) {
            updateStatus()
        }
        password.observe(observingManager) {
            updateStatus()
        }
    }

    /** 各プロパティの値を、それらが依存する他のプロパティの現在の値に合わせて更新する。 */
    // 「増補改訂版 Java 言語で学ぶデザインパターン入門（結城浩著）」の「16章 Mediator」での
    // サンプルプログラムにある colleagueChanged メソッドに相当する。
    private fun updateStatus() {
        when (userType.value) {
            UserType.GUEST -> {
                _isUserIdEnabled.value = false
                _isPasswordEnabled.value = false
                _okEnabled.value = true
            }
            UserType.REGISTERED -> {
                _isUserIdEnabled.value = true
                if (userId.value.isNullOrEmpty()) {
                    _isPasswordEnabled.value = false
                    _okEnabled.value = false
                } else {
                    _isPasswordEnabled.value = true
                    _okEnabled.value = password.value.isNullOrEmpty().not()
                }
            }
            null -> Unit
        }.exhaustive
    }

    /** ログインし、ログイン後の画面に遷移する。 */
    fun login() {
        when (userType.value) {
            UserType.GUEST -> {
                viewModelScope.launch(Dispatchers.Main) {
                    loginRepository.loginGuest()

                    Message.Succeeded(LoginRepository.GUEST_USER_ID).also {
                        _message.emit(it)
                    }
                }
            }
            UserType.REGISTERED -> {
                val userId = checkNotNull(userId.value) { "ユーザーID が null です。" }
                    .also { check(it.isNotEmpty()) { "ユーザーID が空です。" } }
                val password = checkNotNull(password.value) { "パスワードが null です。" }
                    .also { check(it.isNotEmpty()) { "パスワードが空です。" } }

                viewModelScope.launch(Dispatchers.Main) {
                    val isSucceeded = loginRepository.loginRegisteredUser(userId, password)
                    if (isSucceeded.not()) {
                        _message.emit(Message.Failed(userId))
                        return@launch
                    }

                    Message.Succeeded(userId).also {
                        _message.emit(it)
                    }
                }
            }
            null -> throw IllegalStateException("ユーザー種別が選択されていません。")
        }.exhaustive
    }

    /** ログインをキャンセルし、ログイン前の画面に遷移する。 */
    fun cancel() {
        viewModelScope.launch(Dispatchers.Main) {
            _message.emit(Message.Cancelled)
        }
    }

    ////////////////////////////////////////////////////////////////
    // ネストされた型
    //

    /** [message] プロパティから送出されるメッセージ。 */
    sealed class Message {
        /** キャンセルされたためログイン前の画面に遷移することを要求するメッセージ。 */
        object Cancelled : Message()

        /** ログインに成功したのでログイン後の画面に遷移することを要求するメッセージ。 */
        data class Succeeded(
            val userId: String
        ) : Message()

        /** ログインに失敗したのでそのことをユーザーに通知することを要求するメッセージ。 */
        data class Failed(
            val userId: String
        ) : Message()
    }
}