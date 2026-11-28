package com.ebookfrenzy.duvproject.ui.entertainer.setup

import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.data.remote.dto.SetupProgressDto
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.BANK
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.EMERGENCY
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.IDENTIFICATION
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.PROFILE
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.STEP_ORDER
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupDashboardViewModel.Companion.YOUTUBE
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress.Stage
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress.StepState.DONE
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress.StepState.NEXT
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupProgress.StepState.TODO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SetupProgressTest {

    private fun setup(done: Set<String>, status: String = "INCOMPLETE", reason: String? = null) = EntertainerSetupDto(
        id = 1, stageName = null, entertainerType = null, yearStarted = null, baseCharge = null, highestCharge = null,
        state = null, cityLga = null, travelsToOtherStates = false, availableFor = null, alternatePhone = null,
        about = null, photoUrl = null, youtubeUrl = null, bankAccount = null, nextOfKin = null,
        professionalContact = null, identification = null, status = status, rejectionReason = reason,
        setupProgress = SetupProgressDto(done.size, 5, STEP_ORDER.associateWith { it in done }),
    )

    @Test
    fun `nothing done starts at the profile`() {
        val p = SetupProgress.from(setup(emptySet()))
        assertEquals(Stage.IN_PROGRESS, p.stage)
        assertEquals(0, p.done)
        assertEquals(0, p.percent)
        assertEquals(PROFILE, p.nextStep)
        assertEquals(listOf(NEXT, TODO, TODO, TODO, TODO), p.steps)
    }

    @Test
    fun `next step is the first unfinished one in dashboard order`() {
        // Steps can be done out of order: the YouTube card can be saved before the emergency contact.
        val p = SetupProgress.from(setup(setOf(PROFILE, BANK, YOUTUBE)))
        assertEquals(3, p.done)
        assertEquals(60, p.percent)
        assertEquals(EMERGENCY, p.nextStep)
        assertEquals(listOf(DONE, DONE, NEXT, DONE, TODO), p.steps)
    }

    @Test
    fun `all done follows the account status`() {
        val all = setOf(PROFILE, BANK, EMERGENCY, YOUTUBE, IDENTIFICATION)
        assertEquals(Stage.UNDER_REVIEW, SetupProgress.from(setup(all, "PENDING_APPROVAL")).stage)
        assertEquals(Stage.APPROVED, SetupProgress.from(setup(all, "APPROVED")).stage)

        val rejected = SetupProgress.from(setup(all, "REJECTED", "ID photo is unclear"))
        assertEquals(Stage.NEEDS_CHANGES, rejected.stage)
        assertEquals("ID photo is unclear", rejected.rejectionReason)
        assertNull(rejected.nextStep)
        assertEquals(100, rejected.percent)
    }

    @Test
    fun `an unfinished account is in progress whatever its status`() {
        // E.g. an approved entertainer whose bank details were cleared would still need to finish that step.
        val p = SetupProgress.from(setup(setOf(PROFILE), "APPROVED", "old reason"))
        assertEquals(Stage.IN_PROGRESS, p.stage)
        assertNull(p.rejectionReason)
    }
}
