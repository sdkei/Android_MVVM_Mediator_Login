package io.github.sdkei.loginmvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sdkei.loginmvvm.model.LoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** ログイン後の画面の [ViewModel]。 */
class AfterLoginViewModel : ViewModel() {

    /** メッセージを送出する [Flow]。 */
    val message: SharedFlow<Message>
        get() = _message
    private val _message = MutableSharedFlow<Message>()

    private val loginRepository = LoginRepository

    /** ログアウトし、ログイン前の画面に遷移する。 */
    fun logout() {
        viewModelScope.launch(Dispatchers.Main) {
            loginRepository.logout()

            _message.emit(Message.Logout)
        }
    }

    ////////////////////////////////////////////////////////////////
    // ネストされた型
    //

    /** [message] プロパティから送出されるメッセージ。 */
    sealed class Message {
        /** ログアウトしたためログイン前の画面に遷移することを要求するメッセージ。 */
        object Logout : Message()
    }
}