package com.ebookfrenzy.duvproject.ui.entertainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.FragmentDashboardBinding
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.BANK
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.EMERGENCY
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.IDENTIFICATION
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.PROFILE
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.YOUTUBE
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDialogFragment
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDialogFragment.Step
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupEvent
import com.ebookfrenzy.duvproject.ui.entertainer.setup.handleSetupEvent
import com.ebookfrenzy.duvproject.ui.entertainer.setup.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

/** Entertainer set-up checklist (Figma "Entertainer's Account Setup > Dashboard"). */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupDashboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            setupStepProfile.setOnClickListener { open(PROFILE) }
            setupStepBank.setOnClickListener { open(BANK) }
            setupStepEmergency.setOnClickListener { open(EMERGENCY) }
            setupStepYoutube.setOnClickListener { open(YOUTUBE) }
            setupStepIdentification.setOnClickListener { open(IDENTIFICATION) }
            setupContinue.setOnClickListener { continueSetup() }
        }
        // Cards opened from here or from the drawer report back after saving, so progress stays current.
        val refresh = { _: String, _: Bundle -> viewModel.load(force = true) }
        childFragmentManager.setFragmentResultListener(SetupDialogFragment.RESULT_SAVED, viewLifecycleOwner, refresh)
        requireActivity().supportFragmentManager
            .setFragmentResultListener(SetupDialogFragment.RESULT_SAVED, viewLifecycleOwner, refresh)

        // Only load errors reach here (the dashboard never saves); show them without blocking the screen.
        collectWhenStarted(viewModel.events) { event ->
            if (event is SetupEvent.ServerError) requireActivity().handleSetupEvent(event)
        }
    }

    /** Back from a step screen: fetch progress again. */
    override fun onResume() {
        super.onResume()
        viewModel.load(force = true)
    }

    /** "Continue Setup": the first unfinished step, or the account's review status when all five are done. */
    private fun continueSetup() {
        val next = viewModel.nextStep()
        if (next != null) return open(next)
        val setup = viewModel.setup.value ?: return
        when (setup.status) {
            "APPROVED" -> requireActivity().showSnackbar(R.string.setup_all_done_approved)
            "REJECTED" -> requireActivity().showSnackbar(
                getString(R.string.setup_rejected, setup.rejectionReason.orEmpty()),
            )
            else -> requireActivity().showSnackbar(R.string.setup_all_done_pending)
        }
    }

    private fun open(step: String) {
        when (step) {
            PROFILE -> findNavController().navigate(R.id.entertainerProfileFragment)
            EMERGENCY -> findNavController().navigate(R.id.emergencyContactFragment)
            BANK -> SetupDialogFragment.show(childFragmentManager, Step.BANK)
            YOUTUBE -> SetupDialogFragment.show(childFragmentManager, Step.YOUTUBE)
            IDENTIFICATION -> SetupDialogFragment.show(childFragmentManager, Step.IDENTIFICATION)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
