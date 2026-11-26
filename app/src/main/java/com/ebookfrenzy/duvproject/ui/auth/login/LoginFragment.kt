package com.ebookfrenzy.duvproject.ui.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.LoginFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.loadingOverlay
import com.ebookfrenzy.duvproject.ui.common.openHome
import com.ebookfrenzy.duvproject.ui.common.setStatusBar
import com.ebookfrenzy.duvproject.ui.common.setStyledLinkText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var loader: LoadingOverlay

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = loadingOverlay()
        binding.loginFragmentLoginButton.setOnClickListener {
            viewModel.onLoginClicked(
                email = binding.loginFragmentEmailAddressEditText.text.toString(),
                password = binding.loginFragmentPasswordEditText.text.toString(),
            )
        }
        binding.loginFragmentForgotPasswordTextView.setOnClickListener { viewModel.onForgotPasswordClicked() }
        binding.loginFragmentNotRegisteredCreateAccountTextView.setStyledLinkText(
            fullText = "Not Registered? create an account",
            styleStart = 15,
            clickableStart = 16,
            linkColor = ContextCompat.getColor(requireContext(), R.color.duv_primary),
        ) { viewModel.onCreateAccountClicked() }

        collectWhenStarted(viewModel.uiState, ::render)
        collectWhenStarted(viewModel.events, ::handleEvent)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setStatusBar(R.color.duv_background, darkIcons = true)
    }

    private fun render(state: LoginUiState) = with(binding) {
        textInputLayout3.error = state.emailError?.let(::getString)
        loginFragmentPasswordTextInput.error = state.passwordError?.let(::getString)
        loginFragmentLoginButton.isEnabled = !state.isLoading
        loader.setLoading(state.isLoading)
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.NavigateToHome -> requireActivity().openHome(event.isEntertainer)
            LoginEvent.NavigateToForgotPassword ->
                findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
            LoginEvent.NavigateToSignUp -> findNavController().navigate(R.id.signUpFragment)
            is LoginEvent.ShowMessage ->
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
