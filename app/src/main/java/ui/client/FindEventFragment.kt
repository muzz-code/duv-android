package ui.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ebookfrenzy.duvproject.databinding.FindEventFragmentBinding

class FindEventFragment : Fragment() {
    // call the variable for the skip clicker 
    private lateinit var skipClick: TextView

    private var _binding: FindEventFragmentBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FindEventFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipClick = binding.findEventFragmentSkipTextView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
