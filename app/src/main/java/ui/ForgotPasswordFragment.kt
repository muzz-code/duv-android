package ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.ForgotPasswordFragmentBinding

class ForgotPasswordFragment : Fragment() {
    private lateinit var sendBtn: Button

    private var _binding: ForgotPasswordFragmentBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = ForgotPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        sendBtn = binding.forgotPasswordSendButton
//
// //        sendBtn.setOnClickListener {
// //            findNavController().navigate(R.id.)
// //        }
//    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = resources.getColor(R.color.white)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
