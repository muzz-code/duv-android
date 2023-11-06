package ui

// import android.text.InputType
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.SignUpFragmentBinding
//import util.UtilityFunctions.spanLoginText
import util.Validator

class SignUpFragment : Fragment() {
    private lateinit var haveAnAccountLoginText: TextView
    private lateinit var textInputEmail: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signUpBtn: Button
//    private val util = UtilityFunctions.spanLoginText()

    private var _binding: SignUpFragmentBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = resources.getColor(R.color.oxblood_main)
        validatTextFieldOnTextChange()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout for the fragment 
        _binding = SignUpFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        haveAnAccountLoginText = binding.signUpFragmentHaveAnAcccountLoginTextView
        textInputEmail = binding.signUpFragmentEmailAddressEditText
        val email = textInputEmail.text.toString()
        passwordInput = binding.signUpFragmentPasswordEditText
        val password = passwordInput.text.toString()
        val phoneNumber = binding.signUpFragmentPhoneEditText.text.toString()
        signUpBtn = binding.signUpOptionsFragmentSignUpButton
        signUpBtn.setOnClickListener {
//            registerUser(email, phoneNumber, password)
            val email = textInputEmail.text.toString()
            passwordInput = binding.signUpFragmentPasswordEditText
            val password = passwordInput.text.toString()
            val phoneNumber = binding.signUpFragmentPhoneEditText.text.toString()
            signUpBtn = binding.signUpOptionsFragmentSignUpButton
            when {
                email.isEmpty() -> {
                    binding.signUpFragmentEmailAddressTextInput.error = getString(R.string.email_cannot_be_empty)
                    return@setOnClickListener
                }
                !Validator.emailValidate(email) -> {
                    binding.signUpFragmentEmailAddressEditText.error = "Invalid Email"
                    binding.signUpFragmentEmailAddressEditText.requestFocus()
                    return@setOnClickListener
                }
                !Validator.mobileValidate(phoneNumber) -> {
                    binding.signUpFragmentPhoneEditText.error = "Invalid phone number"
                    binding.signUpFragmentPhoneEditText.requestFocus()
                    return@setOnClickListener
                }
                !Validator.passwordValidate(password) -> {
                    binding.signUpFragmentPasswordEditText.error = "Password length should be greater than 5"
                    binding.signUpFragmentPasswordEditText.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    if (validatTextFieldOnTextChange()) {
                        Toast.makeText(
                            requireContext(),
                            "Good to go", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        spanLoginText()
    }

    private fun validatTextFieldOnTextChange(): Boolean {
        var isValidated = true
        binding.signUpFragmentEmailAddressEditText.doOnTextChanged { text, start, before, count ->
            when {
                binding.signUpFragmentEmailAddressEditText.text.toString().trim().isEmpty() -> {
                    binding.signUpFragmentEmailAddressTextInput.error = getString(R.string.email_cannot_be_empty)
                    isValidated = false
                }
                !Validator.emailValidate(binding.signUpFragmentEmailAddressEditText.text.toString().trim()) -> {
                    binding.signUpFragmentEmailAddressTextInput.error = getString(R.string.invalid_email)
                    isValidated = false
                }
                else -> {
                    binding.signUpFragmentEmailAddressTextInput.error = null
                    isValidated = true
                }
            }
        }

        binding.signUpFragmentPhoneEditText.doOnTextChanged { text, start, before, count ->
            when {
                binding.signUpFragmentPhoneEditText.text.toString().trim().isEmpty() -> {
                    binding.signUpFragmentPhoneTextInput.error = getString(R.string.phone_number_cannot_be_empty)
                    isValidated = false
                }
                !Validator.mobileValidate(binding.signUpFragmentPhoneEditText.text.toString().trim()) -> {
                    binding.signUpFragmentPhoneTextInput.error = getString(R.string.invalid_phone_number)
                    isValidated = false
                }
                else -> {
                    binding.signUpFragmentPhoneTextInput.error = null
                    isValidated = true
                }
            }
        }
        binding.signUpFragmentPasswordEditText.doOnTextChanged { text, start, before, count ->
            when {
                binding.signUpFragmentPasswordEditText.text.toString().trim().isEmpty() -> {
                    binding.signUpFragmentPasswordTextInput.error = getString(R.string.pasword_cannot_be_empty)
                    isValidated = false
                }
                !Validator.passwordValidate(binding.signUpFragmentPasswordEditText.text.toString().trim()) -> {
                    binding.signUpFragmentPasswordTextInput.error = getString(R.string.minimum_of_5_xter)
                    isValidated = false
                }
                else -> {
                    binding.signUpFragmentPasswordTextInput.error = null
                    isValidated = true
                }
            }
        }

        binding.signUpFragmentConfirmPasswordEditText.doOnTextChanged { text, start, before, count ->
            when {
                binding.signUpFragmentConfirmPasswordEditText.toString().trim().isEmpty() -> {
                    binding.signUpFragmentConfirmPasswordTextInput.error = getString(R.string.confirm_password_cannot_be_empty)
                    isValidated = false
                }
                binding.signUpFragmentPasswordEditText.text.toString().trim() != binding.signUpFragmentConfirmPasswordEditText.text.toString().trim() -> {
                    binding.signUpFragmentConfirmPasswordTextInput.error = getString(R.string.password_does_not_match)
                    isValidated = false
                }
                else -> {
                    binding.signUpFragmentConfirmPasswordTextInput.error = null
                    isValidated = true
                }
            }
        }

        return isValidated
    }

    private fun spanLoginText() {
        // set the spannable text
        val spannableString = SpannableString("Have an account?  Login")

        // set the spannable image
        val fColor = ForegroundColorSpan(Color.WHITE)
        val sizeSpan = RelativeSizeSpan(1.1f)
        val styleSpan = StyleSpan(Typeface.BOLD)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(requireContext(), "nice one", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        spannableString.setSpan(clickableSpan, 18, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(fColor, 18, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(sizeSpan, 18, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(styleSpan, 18, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        haveAnAccountLoginText.text = spannableString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
