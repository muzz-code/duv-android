package com.ebookfrenzy.duvproject.ui.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.SignUpFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.setStyledLinkText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: SignUpFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SignUpFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signUpFragmentEmailAddressEditText.doAfterTextChanged { viewModel.onEmailChanged(it.toString()) }
            signUpFragmentPhoneEditText.doAfterTextChanged { viewModel.onPhoneChanged(it.toString()) }
            signUpFragmentPasswordEditText.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }
            signUpFragmentConfirmPasswordEditText.doAfterTextChanged {
                viewModel.onConfirmPasswordChanged(it.toString())
            }

            signUpOptionsFragmentSignUpButton.setOnClickListener {
                viewModel.onSignUpClicked(
                    email = signUpFragmentEmailAddressEditText.text.toString(),
                    phone = signUpFragmentPhoneEditText.text.toString(),
                    password = signUpFragmentPasswordEditText.text.toString(),
                    confirmPassword = signUpFragmentConfirmPasswordEditText.text.toString(),
                )
            }

            signUpFragmentHaveAnAcccountLoginTextView.setStyledLinkText(
                fullText = "Have an account?  Login",
                styleStart = 18,
                clickableStart = 18,
            ) { viewModel.onLoginLinkClicked() }
        }

        collectWhenStarted(viewModel.uiState, ::render)
        collectWhenStarted(viewModel.events, ::handleEvent)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.oxblood_main)
    }

    private fun render(state: SignUpUiState) = with(binding) {
        signUpFragmentEmailAddressTextInput.showError(state.emailError)
        signUpFragmentPhoneTextInput.showError(state.phoneError)
        signUpFragmentPasswordTextInput.showError(state.passwordError)
        signUpFragmentConfirmPasswordTextInput.showError(state.confirmPasswordError)
    }

    private fun handleEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.ShowMessage ->
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            // Same placeholder as before: the "Login" link only showed a message.
            SignUpEvent.LoginLinkClicked ->
                Toast.makeText(requireContext(), "nice one", Toast.LENGTH_SHORT).show()
        }
    }

    private fun TextInputLayout.showError(messageRes: Int?) {
        error = messageRes?.let(::getString)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
