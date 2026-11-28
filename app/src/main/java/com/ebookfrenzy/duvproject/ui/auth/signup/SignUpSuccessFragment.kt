package com.ebookfrenzy.duvproject.ui.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.SignUpSuccessFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.openHome
import com.ebookfrenzy.duvproject.ui.common.setStatusBar

/**
 * Shown after a successful sign-up. The account is already created and the user is signed in,
 * so both "Continue" and Back take them to their home screen rather than back to the form.
 */
class SignUpSuccessFragment : Fragment() {

    private var _binding: SignUpSuccessFragmentBinding? = null
    private val binding get() = _binding!!

    private val isEntertainer: Boolean
        get() = requireArguments().getBoolean(ARG_IS_ENTERTAINER)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SignUpSuccessFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signUpSuccessMessage.setText(
            if (isEntertainer) R.string.account_created_message_entertainer else R.string.account_created_message_user
        )
        binding.signUpSuccessContinueButton.setOnClickListener { goHome() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { goHome() }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setStatusBar(R.color.white, darkIcons = true)
    }

    private fun goHome() = requireActivity().openHome(isEntertainer)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_IS_ENTERTAINER = "isEntertainer"

        fun args(isEntertainer: Boolean) = bundleOf(ARG_IS_ENTERTAINER to isEntertainer)
    }
}
