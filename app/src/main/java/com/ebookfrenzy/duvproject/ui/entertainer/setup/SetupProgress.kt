package com.ebookfrenzy.duvproject.ui.entertainer.setup

import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.STEP_ORDER

/**
 * What the dashboard's progress card and step rows show (Figma "Account set-up progress (proposal)"), worked out from
 * the profile the backend returns. Kept free of Android types so it can be unit tested.
 */
data class SetupProgress(
    val stage: Stage,
    /** Steps done, 0..[total]. */
    val done: Int,
    val total: Int,
    /** One entry per step, in [STEP_ORDER] order. */
    val steps: List<StepState>,
    /** The step "Continue Setup" opens, or null once all are done. */
    val nextStep: String?,
    /** Why DUV refused the account ([Stage.NEEDS_CHANGES] only). */
    val rejectionReason: String?,
) {
    enum class Stage { IN_PROGRESS, UNDER_REVIEW, APPROVED, NEEDS_CHANGES }

    enum class StepState { DONE, NEXT, TODO }

    val percent: Int get() = if (total == 0) 0 else done * 100 / total

    companion object {
        fun from(setup: EntertainerSetupDto): SetupProgress {
            val finished = setup.setupProgress.steps
            val nextStep = STEP_ORDER.firstOrNull { finished[it] != true }
            val steps = STEP_ORDER.map { step ->
                when {
                    finished[step] == true -> StepState.DONE
                    step == nextStep -> StepState.NEXT
                    else -> StepState.TODO
                }
            }
            val done = steps.count { it == StepState.DONE }
            // The backend's status decides the stage once everything is in; until then it's always "in progress".
            val stage = when {
                nextStep != null -> Stage.IN_PROGRESS
                setup.status == STATUS_APPROVED -> Stage.APPROVED
                setup.status == STATUS_REJECTED -> Stage.NEEDS_CHANGES
                else -> Stage.UNDER_REVIEW
            }
            return SetupProgress(
                stage = stage,
                done = done,
                total = STEP_ORDER.size,
                steps = steps,
                nextStep = nextStep,
                rejectionReason = setup.rejectionReason?.takeIf { stage == Stage.NEEDS_CHANGES && it.isNotBlank() },
            )
        }

        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
    }
}
