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
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.repository.AccountCategory
import com.ebookfrenzy.duvproject.databinding.SignUpFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.loadingOverlay
import com.ebookfrenzy.duvproject.ui.common.openHome
import com.ebookfrenzy.duvproject.ui.common.setStyledLinkText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: SignUpFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var loader: LoadingOverlay

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SignUpFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = loadingOverlay()
        with(binding) {
            signUpFragmentEmailAddressEditText.doAfterTextChanged { viewModel.onEmailChanged(it.toString()) }
            signUpFragmentPhoneEditText.doAfterTextChanged { viewModel.onPhoneChanged(it.toString()) }
            signUpFragmentPasswordEditText.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }
            signUpFragmentConfirmPasswordEditText.doAfterTextChanged {
                viewModel.onConfirmPasswordChanged(it.toString())
            }

            // "Choose category" opens a list instead of the keyboard, so only valid categories can be sent.
            signUpFragmentChooseCategoryEditText.apply {
                isFocusable = false
                isCursorVisible = false
                setOnClickListener { viewModel.onCategoryFieldClicked() }
            }

            signUpOptionsFragmentSignUpButton.setOnClickListener {
                viewModel.onSignUpClicked(
                    firstName = signUpFragmentFirstNameEditText.text.toString(),
                    lastName = signUpFragmentLastNameEditText.text.toString(),
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
        val server = state.serverFieldErrors
        signUpFragmentFirstNameTextInput.showError(state.firstNameError, server["firstName"])
        signUpFragmentLastNameTextInput.showError(state.lastNameError, server["lastName"])
        signUpFragmentEmailAddressTextInput.showError(state.emailError, server["email"])
        signUpFragmentChooseCategoryTextInput.showError(state.categoryError, server["category"])
        signUpFragmentPhoneTextInput.showError(state.phoneError, server["phone"])
        signUpFragmentPasswordTextInput.showError(state.passwordError, server["password"])
        signUpFragmentConfirmPasswordTextInput.showError(state.confirmPasswordError)

        state.category?.let { signUpFragmentChooseCategoryEditText.setText(it.label) }

        signUpOptionsFragmentSignUpButton.isEnabled = !state.isLoading
        loader.setLoading(state.isLoading)
    }

    private fun handleEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.ShowMessage ->
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
            SignUpEvent.ChooseCategory -> showCategoryDialog()
            is SignUpEvent.NavigateToHome -> requireActivity().openHome(event.isEntertainer)
            SignUpEvent.NavigateToLogin -> findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun showCategoryDialog() {
        val categories = AccountCategory.entries
        val current = categories.indexOf(viewModel.uiState.value.category)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_category)
            .setSingleChoiceItems(categories.map { it.label }.toTypedArray(), current) { dialog, which ->
                viewModel.onCategoryChosen(categories[which])
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** Local check first; otherwise whatever the backend said about this field. */
    private fun TextInputLayout.showError(messageRes: Int?, serverMessage: String? = null) {
        error = messageRes?.let(::getString) ?: serverMessage
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
