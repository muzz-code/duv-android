package ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.LoginSignUpOptionsFragmentBinding

class LoginSignUpOptionsFragment : Fragment() {
    private var _binding: LoginSignUpOptionsFragmentBinding? = null
    val binding get() = _binding!!

    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate layout here
        _binding = LoginSignUpOptionsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initialize the buttons
        loginBtn = binding.loginSignUpOptionsFragmentLoginButton
        signUpBtn = binding.loginSignUpOptionsFragmentSignUpButton

        loginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginSignUpOptionsFragment_to_loginFragment)
        }
        signUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginSignUpOptionsFragment_to_signUpFragment)
        }
    }
    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = resources.getColor(R.color.oxblood_main)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
