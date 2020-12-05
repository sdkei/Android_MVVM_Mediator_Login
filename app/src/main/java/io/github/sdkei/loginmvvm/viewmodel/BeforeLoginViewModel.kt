package io.github.sdkei.loginmvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** ログイン前の画面の [ViewModel]。 */
class BeforeLoginViewModel : ViewModel() {

    /** メッセージを送出する [Flow]。 */
    val message: SharedFlow<Message>
        get() = _message
    private val _message = MutableSharedFlow<Message>()

    /** ログイン画面に遷移する。 */
    fun login() {
        viewModelScope.launch(Dispatchers.Main) {
            _message.emit(Message.Login)
        }
    }

    ////////////////////////////////////////////////////////////////
    // ネストされた型
    //

    /** [message] プロパティから送出されるメッセージ。 */
    sealed class Message {
        /** ログイン画面に遷移することを要求するメッセージ。 */
        object Login : Message()
    }
}