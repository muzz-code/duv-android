package com.ebookfrenzy.duvproject.ui.auth.login

import android.content.Intent
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
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.setStyledLinkText
import com.ebookfrenzy.duvproject.ui.entertainer.EntertainerActivity
import com.readystatesoftware.systembartint.SystemBarTintManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val oxblood = ContextCompat.getColor(requireContext(), R.color.oxblood_main)
        SystemBarTintManager(requireActivity()).apply {
            isStatusBarTintEnabled = true
            setTintColor(oxblood)
        }

        binding.loginFragmentLoginButton.setOnClickListener { viewModel.onLoginClicked() }
        binding.loginFragmentForgotPasswordTextView.setOnClickListener { viewModel.onForgotPasswordClicked() }
        binding.loginFragmentNotRegisteredCreateAccountTextView.setStyledLinkText(
            fullText = "Not Registered? create an account",
            styleStart = 15,
            clickableStart = 16,
        ) {
            Toast.makeText(requireContext(), "e work", Toast.LENGTH_SHORT).show()
        }

        collectWhenStarted(viewModel.events, ::handleEvent)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.oxblood_main)
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.NavigateToHome ->
                startActivity(Intent(requireActivity(), EntertainerActivity::class.java))
            LoginEvent.NavigateToForgotPassword ->
                findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
