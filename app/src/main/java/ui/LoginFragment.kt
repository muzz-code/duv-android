package ui

import android.annotation.SuppressLint
import android.content.Intent
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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.LoginFragmentBinding
import com.readystatesoftware.systembartint.SystemBarTintManager
import ui.client.EntertainerActivity

class LoginFragment : Fragment() {
    private lateinit var forgotPasswordTextKey: TextView
    private lateinit var createAnAccountTextView: TextView
    private lateinit var loginBtn: Button

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = resources.getColor(R.color.oxblood_main)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginBtn = binding.loginFragmentLoginButton
        createAnAccountTextView = binding.loginFragmentNotRegisteredCreateAccountTextView
        forgotPasswordTextKey = binding.loginFragmentForgotPasswordTextView
        forgotPasswordTextKey.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        textSpan()

//        val window = fragment.window
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.statusBarColor = ContextCompat.getColor(activity!!, R.color.example_color)

        val mTintManager = SystemBarTintManager(requireActivity())
// enable status bar tint
// enable status bar tint
        mTintManager.isStatusBarTintEnabled = true
        mTintManager.setTintColor(resources.getColor(R.color.oxblood_main))

        loginBtn.setOnClickListener {
            val intent = Intent(requireActivity(), EntertainerActivity::class.java)
            startActivity(intent)
        }
    }

    // styling my text view with a spannable
    @SuppressLint("ResourceAsColor")
    fun textSpan() {
        // set the spannable string
        val spannableString = SpannableString("Not Registered? create an account")

        // set the spannable functions
        val fColor = ForegroundColorSpan(Color.WHITE)
        val sizeSpan = RelativeSizeSpan(1.1f)
        val styleSpan = StyleSpan(Typeface.BOLD)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(requireContext(), "e work", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        // implement the spannable functions on the spannable string
        spannableString.setSpan(clickableSpan, 16, 33, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(fColor, 15, 33, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(sizeSpan, 15, 33, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(styleSpan, 15, 33, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        createAnAccountTextView.text = spannableString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
