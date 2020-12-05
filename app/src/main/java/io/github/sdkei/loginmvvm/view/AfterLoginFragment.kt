package io.github.sdkei.loginmvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View =
        AfterLoginFragmentBinding.inflate(inflater, container, false).also { binding ->
            binding.viewModel =
                ViewModelProvider(this).get(AfterLoginViewModel::class.java).also { viewModel ->
                    viewModel.message.onEach {
                        onMessage(it)
                    }.launchIn(lifecycleScope)
                }
            binding.lifecycleOwner = viewLifecycleOwner
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
