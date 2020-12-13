package io.github.sdkei.loginmvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.github.sdkei.loginmvvm.R
import io.github.sdkei.loginmvvm.databinding.AfterLoginFragmentBinding
import io.github.sdkei.loginmvvm.utils.exhaustive
import io.github.sdkei.loginmvvm.viewmodel.AfterLoginViewModel
import io.github.sdkei.loginmvvm.viewmodel.AfterLoginViewModel.Message
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** ログイン後の画面。 */
class AfterLoginFragment : Fragment() {
    private val viewModel by viewModels<AfterLoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View =
        AfterLoginFragmentBinding.inflate(inflater, container, false).also {
            it.viewModel = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }.also {
            // メッセージの監視を開始する。
            // このタイミングである必要はないが、
            // データバインディングとタイミングを合わせておく。
            viewModel.message
                .onEach { onMessage(it) }
                .launchIn(lifecycleScope)
            // ↑lifecycleScope は Dispatchers.Main に束縛されているため、
            // ↑onMessage 関数はメインスレッドで呼び出される。
        }.root

    /** [ViewModel] から送られたメッセージを処理する。 */
    private fun onMessage(message: Message) {
        when (message) {
            is Message.Logout -> onMessageLogout(message)
        }.exhaustive
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onMessageLogout(message: Message.Logout) {
        findNavController().navigate(R.id.action_afterLoginFragment_to_beforeLoginFragment)
    }
}
