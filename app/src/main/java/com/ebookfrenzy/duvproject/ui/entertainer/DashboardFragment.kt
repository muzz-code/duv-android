package com.ebookfrenzy.duvproject.ui.entertainer

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.databinding.FragmentDashboardBinding
import com.ebookfrenzy.duvproject.databinding.SetupStepRowBinding
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
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress.Stage
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress.StepState
import com.ebookfrenzy.duvproject.ui.entertainer.setup.handleSetupEvent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Entertainer set-up checklist (Figma "Entertainer's Account Setup > Dashboard"), with the progress card and step
 * status from Figma "Account set-up progress (proposal)".
 */
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
        stepRows().forEachIndexed { index, (step, row) ->
            row.stepName.setText(STEP_NAMES.getValue(step))
            row.stepNumber.text = (index + 1).toString()   // shown until the profile loads
            row.root.setOnClickListener { open(step) }
        }
        binding.setupContinue.setOnClickListener { continueSetup() }
        collectWhenStarted(viewModel.setup) { setup -> setup?.let { show(SetupProgress.from(it)) } }
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

    /** "Continue Setup" opens the first unfinished step; "Update My Details" (after a rejection) opens the profile. */
    private fun continueSetup() {
        open(viewModel.nextStep() ?: PROFILE)
    }

    private fun stepRows(): List<Pair<String, SetupStepRowBinding>> = with(binding) {
        listOf(
            PROFILE to setupStepProfile, BANK to setupStepBank, EMERGENCY to setupStepEmergency,
            YOUTUBE to setupStepYoutube, IDENTIFICATION to setupStepIdentification,
        )
    }

    private fun show(progress: SetupProgress) = with(binding) {
        val stage = STAGES.getValue(progress.stage)
        setupTitle.setText(stage.title)
        setupIntro.isVisible = progress.stage == Stage.IN_PROGRESS

        setupProgressCard.isVisible = true
        setupProgressStage.setText(stage.label)
        setupProgressStage.setTextColor(color(stage.labelText))
        setupProgressStage.backgroundTintList = ColorStateList.valueOf(color(stage.labelBackground))
        setupProgressCount.text = getString(R.string.setup_progress_count, progress.done, progress.total)
        setupProgressPercent.text = getString(R.string.setup_progress_percent, progress.percent)
        setupProgressBar.children.forEachIndexed { index, segment ->
            val done = index < progress.done
            segment.backgroundTintList = ColorStateList.valueOf(
                color(if (!done) R.color.setup_progress_track else stage.bar),
            )
        }
        setupProgressHelper.text = when (progress.stage) {
            Stage.IN_PROGRESS -> getString(R.string.setup_next_up, getString(STEP_NAMES.getValue(progress.nextStep!!)))
            Stage.UNDER_REVIEW -> getString(R.string.setup_helper_under_review)
            Stage.APPROVED -> getString(R.string.setup_helper_approved)
            Stage.NEEDS_CHANGES -> progress.rejectionReason?.let { getString(R.string.setup_helper_needs_changes, it) }
                ?: getString(R.string.setup_helper_needs_changes_no_reason)
        }
        setupProgressHelper.setTextColor(
            color(if (progress.stage == Stage.NEEDS_CHANGES) R.color.setup_status_error else R.color.setup_progress_muted),
        )
        setupProgressCard.contentDescription = listOf(
            getString(R.string.setup_progress_label), setupProgressStage.text, setupProgressCount.text,
            setupProgressHelper.text,
        ).joinToString(". ")

        stepRows().forEachIndexed { index, (step, row) -> showStep(row, index + 1, step, progress.steps[index]) }

        // Nothing to continue once everything is in, unless DUV asked for changes.
        setupContinue.isVisible = stage.button != null
        stage.button?.let(setupContinue::setText)
    }

    private fun showStep(row: SetupStepRowBinding, number: Int, step: String, state: StepState) {
        val name = getString(STEP_NAMES.getValue(step))
        row.root.setBackgroundResource(if (state == StepState.NEXT) R.drawable.bg_setup_field_next else R.drawable.bg_setup_field)
        row.stepDoneIcon.isVisible = state == StepState.DONE
        row.stepNumber.isVisible = state != StepState.DONE
        row.stepNumber.text = number.toString()
        val ring = if (state == StepState.NEXT) R.color.setup_button else R.color.setup_field_border
        row.stepNumber.backgroundTintList = ColorStateList.valueOf(color(ring))
        row.stepNumber.setTextColor(color(if (state == StepState.NEXT) R.color.setup_button else R.color.setup_progress_muted))
        row.stepStatus.isVisible = state != StepState.TODO
        when (state) {
            StepState.DONE -> {
                row.stepStatus.setText(R.string.setup_step_done)
                row.stepStatus.setTextColor(color(R.color.setup_status_ok))
            }
            StepState.NEXT -> {
                row.stepStatus.setText(R.string.setup_step_next)
                row.stepStatus.setTextColor(color(R.color.setup_button))
            }
            StepState.TODO -> Unit
        }
        row.root.contentDescription = getString(
            when (state) {
                StepState.DONE -> R.string.setup_step_done_description
                StepState.NEXT -> R.string.setup_step_next_description
                StepState.TODO -> R.string.setup_step_todo_description
            },
            name,
        )
    }

    private fun color(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

    private fun open(step: String) {
        when (step) {
            PROFILE -> findNavController().navigate(R.id.entertainerProfileFragment)
            EMERGENCY -> findNavController().navigate(R.id.emergencyContactFragment)
            BANK -> SetupDialogFragment.show(childFragmentManager, Step.BANK)
            YOUTUBE -> SetupDialogFragment.show(childFragmentManager, Step.YOUTUBE)
            IDENTIFICATION -> SetupDialogFragment.show(childFragmentManager, Step.IDENTIFICATION)
        }
    }

    /** Title, status label colours, bar colour and button for each stage. */
    private class StageStyle(
        @StringRes val title: Int,
        @StringRes val label: Int,
        @ColorRes val labelText: Int,
        @ColorRes val labelBackground: Int,
        @ColorRes val bar: Int,
        @StringRes val button: Int?,
    )

    private companion object {
        val STEP_NAMES = mapOf(
            PROFILE to R.string.entertainer_profile, BANK to R.string.bank_account_details,
            EMERGENCY to R.string.emergency_contact, YOUTUBE to R.string.youtube_channel,
            IDENTIFICATION to R.string.valid_identification,
        )

        val STAGES = mapOf(
            Stage.IN_PROGRESS to StageStyle(
                R.string.setup_title, R.string.setup_stage_in_progress, R.color.setup_chip_progress_text,
                R.color.setup_chip_progress_bg, R.color.setup_button, R.string.setup_continue,
            ),
            Stage.UNDER_REVIEW to StageStyle(
                R.string.setup_title_under_review, R.string.setup_stage_under_review, R.color.setup_chip_review_text,
                R.color.setup_chip_review_bg, R.color.setup_button, null,
            ),
            Stage.APPROVED to StageStyle(
                R.string.setup_title_approved, R.string.setup_stage_approved, R.color.setup_status_ok,
                R.color.setup_chip_approved_bg, R.color.setup_status_ok, null,
            ),
            Stage.NEEDS_CHANGES to StageStyle(
                R.string.setup_title_needs_changes, R.string.setup_stage_needs_changes, R.color.setup_status_error,
                R.color.setup_chip_changes_bg, R.color.setup_button, R.string.setup_update_details,
            ),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
