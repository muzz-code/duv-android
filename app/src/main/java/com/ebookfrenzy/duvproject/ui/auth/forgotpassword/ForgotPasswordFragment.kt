package com.ebookfrenzy.duvproject.ui.auth.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.ForgotPasswordFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.loadingOverlay
import com.ebookfrenzy.duvproject.ui.common.setStatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: ForgotPasswordFragmentBinding? = null
    val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()
    private lateinit var loader: LoadingOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ForgotPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = loadingOverlay()
        // The email box's id is login_fragment_password_edit_text in this layout (kept so the layout is unchanged).
        binding.forgotPasswordSendButton.setOnClickListener {
            viewModel.onSendClicked(binding.loginFragmentPasswordEditText.text.toString())
        }
        binding.textView.setOnClickListener { viewModel.onBackToLoginClicked() }

        collectWhenStarted(viewModel.uiState, ::render)
        collectWhenStarted(viewModel.events, ::handleEvent)
    }

    private fun render(state: ForgotPasswordUiState) = with(binding) {
        forgotPasswordFragmentPasswordTextInput.error = state.emailError?.let(::getString)
        forgotPasswordSendButton.isEnabled = !state.isLoading
        loader.setLoading(state.isLoading)
    }

    private fun handleEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.LinkSent -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            is ForgotPasswordEvent.ShowMessage ->
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
            ForgotPasswordEvent.NavigateBack -> findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setStatusBar(R.color.white, darkIcons = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
