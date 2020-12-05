package io.github.sdkei.loginmvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.github.sdkei.loginmvvm.R
import io.github.sdkei.loginmvvm.databinding.LoginFragmentBinding
import io.github.sdkei.loginmvvm.utils.exhaustive
import io.github.sdkei.loginmvvm.viewmodel.LoginViewModel
import io.github.sdkei.loginmvvm.viewmodel.LoginViewModel.Message
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** ログイン画面。 */
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View =
        LoginFragmentBinding.inflate(inflater, container, false).also { binding ->
            binding.viewModel =
                ViewModelProvider(this).get(LoginViewModel::class.java).also { viewModel ->
                    viewModel.message.onEach {
                        onMessage(it)
                    }.launchIn(lifecycleScope)
                }
            binding.lifecycleOwner = viewLifecycleOwner
        }.root

    /** [ViewModel] から送られたメッセージを処理する。 */
    private fun onMessage(message: Message) {
        when (message) {
            is Message.Cancelled -> onMessageCancelled(message)
            is Message.Succeeded -> onMessageSucceeded(message)
            is Message.Failed -> onMessageFailed(message)
        }.exhaustive
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onMessageCancelled(message: Message.Cancelled) {
        findNavController().navigate(R.id.action_loginFragment_to_beforeLoginFragment)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onMessageSucceeded(message: Message.Succeeded) {
        findNavController().navigate(R.id.action_loginFragment_to_afterLoginFragment)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onMessageFailed(message: Message.Failed) {
        // ログインできなかったことを通知するダイアログを表示する。
        lifecycleScope.launchWhenResumed {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.dialog_message_login_failed)
                .setPositiveButton(R.string.button_close) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }
}