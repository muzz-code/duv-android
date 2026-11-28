package com.ebookfrenzy.duvproject.ui.entertainer.setup

import com.ebookfrenzy.duvproject.MainDispatcherRule
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.remote.dto.BankDto
import com.ebookfrenzy.duvproject.data.remote.dto.ContactDto
import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.data.remote.dto.IdentificationDto
import com.ebookfrenzy.duvproject.data.remote.dto.ProfileDetailsRequest
import com.ebookfrenzy.duvproject.data.remote.dto.ResolvedAccountDto
import com.ebookfrenzy.duvproject.data.remote.dto.SetupProgressDto
import com.ebookfrenzy.duvproject.data.repository.EntertainerSetupRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SetupViewModelsTest {

    @get:Rule val mainRule = MainDispatcherRule()

    /** Records what would be sent and answers with a profile whose progress the test controls. */
    private class FakeSetupRepository(var steps: Map<String, Boolean> = emptyMap()) : EntertainerSetupRepository {
        var profile: ProfileDetailsRequest? = null
        var contacts: Pair<ContactDto, ContactDto?>? = null
        var identification: IdentificationDto? = null
        /** (bank code, account number) of the last save. */
        var bank: Pair<String, String>? = null
        var lookups = 0
        var youtube: String? = null

        fun setup() = EntertainerSetupDto(
            id = 1, stageName = null, entertainerType = null, yearStarted = null, baseCharge = null,
            highestCharge = null, state = null, cityLga = null, travelsToOtherStates = false, availableFor = null,
            alternatePhone = null, about = null, photoUrl = null, youtubeUrl = null, bankAccount = null,
            nextOfKin = null, professionalContact = null, identification = null, status = "INCOMPLETE",
            rejectionReason = null, setupProgress = SetupProgressDto(steps.count { it.value }, 5, steps),
        )

        private fun ok() = ApiResult.Success(setup())
        override suspend fun load() = ok()
        override suspend fun saveProfile(request: ProfileDetailsRequest) = ok().also { profile = request }
        override suspend fun uploadPhoto(jpeg: ByteArray) = ok()
        override suspend fun downloadPhoto(path: String) = ApiResult.Success(ByteArray(0))
        override suspend fun saveIdentification(details: IdentificationDto) = ok().also { identification = details }
        override suspend fun saveBankAccount(bankCode: String, accountNumber: String) =
            ok().also { bank = bankCode to accountNumber }
        override suspend fun banks() = ApiResult.Success(listOf(BankDto("058", "Guaranty Trust Bank"), BankDto("057", "Zenith Bank")))
        /** Like the bank: numbers starting 000 don't exist. */
        override suspend fun resolveAccount(bankCode: String, accountNumber: String) = also { lookups++ }.let {
            if (accountNumber.startsWith("000")) ApiResult.Failure("We couldn't find this account.")
            else ApiResult.Success(ResolvedAccountDto(bankCode, "Bank $bankCode", accountNumber, "ADA OBI"))
        }
        override suspend fun saveEmergencyContacts(nextOfKin: ContactDto, professional: ContactDto?) =
            ok().also { contacts = nextOfKin to professional }
        override suspend fun saveYoutube(url: String) = ok().also { youtube = url }
    }

    private fun form(base: String = "₦100,000", highest: String = "₦500,000") = ProfileForm(
        stageName = " DJ Ada ", type = "Live band", state = "FCT Abuja", lga = "Bwari", baseCharge = base,
        highestCharge = highest, travel = "Yes", year = "2018", availableFor = "Wedding, Club", about = "  ",
    )

    // ---- Entertainer Profile ----

    @Test
    fun `profile labels are sent as backend values`() = runTest {
        val repo = FakeSetupRepository()
        val vm = ProfileSetupViewModel(repo)

        vm.saveProfile(form())

        val sent = repo.profile!!
        assertEquals("DJ Ada", sent.stageName)
        assertEquals("LIVE_BAND", sent.entertainerType)
        assertEquals(100_000L, sent.baseCharge)
        assertEquals(500_000L, sent.highestCharge)
        assertEquals(true, sent.travelsToOtherStates)
        assertEquals(listOf("WEDDING", "CLUB"), sent.availableFor)
        assertNull("blank About is sent as null", sent.about)
        assertEquals(SetupEvent.Saved(R.string.setup_saved_profile), vm.events.first())
    }

    @Test
    fun `base charge above highest charge is refused before sending`() = runTest {
        val repo = FakeSetupRepository()
        val vm = ProfileSetupViewModel(repo)

        vm.saveProfile(form(base = "₦500,000", highest = "₦100,000"))

        assertNull(repo.profile)
        assertEquals(SetupEvent.Invalid(R.string.setup_error_charge_range), vm.events.first())
    }

    // ---- Emergency Contact ----

    private val kin = ContactForm("Ada", "Obi", "", "0805 123 4567", "Sibling")

    @Test
    fun `next of kin alone is saved without a professional contact`() = runTest {
        val repo = FakeSetupRepository()
        val vm = EmergencyContactViewModel(repo)

        vm.saveContacts(kin, ContactForm("", "", "", "", ""))

        val (sentKin, sentPro) = repo.contacts!!
        assertEquals("08051234567", sentKin.phone)
        assertNull(sentKin.email)
        assertNull(sentPro)
    }

    @Test
    fun `a half-filled professional contact is refused`() = runTest {
        val repo = FakeSetupRepository()
        val vm = EmergencyContactViewModel(repo)

        vm.saveContacts(kin, ContactForm("Tunde", "", "", "", ""))

        assertNull(repo.contacts)
        assertEquals(SetupEvent.Invalid(R.string.setup_error_professional_incomplete), vm.events.first())
    }

    @Test
    fun `an invalid phone number is refused`() = runTest {
        val repo = FakeSetupRepository()
        val vm = EmergencyContactViewModel(repo)

        vm.saveContacts(kin.copy(phone = "12345"), ContactForm("", "", "", "", ""))

        assertNull(repo.contacts)
        assertEquals(SetupEvent.Invalid(R.string.invalid_phone_number), vm.events.first())
    }

    // ---- Pop-up cards ----

    private fun ui(date: LocalDate) = date.format(SetupOptions.uiDate)

    @Test
    fun `identification dates are sent as ISO and expiry is optional`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo)
        val issued = LocalDate.now().minusYears(1)

        vm.saveIdentification("NIN slip", " ab123 456 ", ui(issued), "")

        assertEquals(IdentificationDto("NIN_SLIP", "AB123 456", issued.toString(), null), repo.identification)
    }

    @Test
    fun `an expired ID is refused`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo)

        vm.saveIdentification(
            "International passport", "A1234567", ui(LocalDate.now().minusYears(6)), ui(LocalDate.now().minusDays(1)),
        )

        assertNull(repo.identification)
        assertEquals(SetupEvent.Invalid(R.string.setup_error_expired), vm.events.first())
    }

    @Test
    fun `bank account number must be 10 digits`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo).apply { loadBanks() }

        vm.saveBank("Guaranty Trust Bank", "12345")
        assertNull(repo.bank)
        assertEquals(SetupEvent.Invalid(R.string.setup_error_account_number), vm.events.first())
    }

    @Test
    fun `the bank fills in the account name and only a confirmed account is saved`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo).apply { loadBanks() }

        // Saving before the bank has answered is refused.
        vm.lookUp("Guaranty Trust Bank", "0123 456 789")
        assertEquals(AccountLookup.Checking, vm.lookup.value)
        vm.saveBank("Guaranty Trust Bank", "0123 456 789")
        assertNull(repo.bank)
        assertEquals(SetupEvent.Invalid(R.string.setup_error_account_not_verified), vm.events.first())

        advanceUntilIdle()
        assertEquals(AccountLookup.Found("058", "0123456789", "ADA OBI"), vm.lookup.value)
        vm.saveBank("Guaranty Trust Bank", "0123 456 789")
        assertEquals("058" to "0123456789", repo.bank)
    }

    @Test
    fun `an account the bank doesn't know can't be saved`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo).apply { loadBanks() }

        vm.lookUp("Zenith Bank", "0001234567")
        advanceUntilIdle()
        assertEquals(AccountLookup.Failed("We couldn't find this account."), vm.lookup.value)
        vm.saveBank("Zenith Bank", "0001234567")
        assertNull(repo.bank)
    }

    @Test
    fun `typing quickly looks the account up once`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo).apply { loadBanks() }

        vm.lookUp("Zenith Bank", "0123456788")
        vm.lookUp("Zenith Bank", "0123456789")   // corrected the last digit before the debounce ran out
        advanceUntilIdle()
        assertEquals(1, repo.lookups)
        assertEquals(AccountLookup.Found("057", "0123456789", "ADA OBI"), vm.lookup.value)
    }

    @Test
    fun `youtube link must be a youtube address`() = runTest {
        val repo = FakeSetupRepository()
        val vm = SetupCardViewModel(repo)

        vm.saveYoutube("https://vimeo.com/ada")
        assertNull(repo.youtube)

        vm.saveYoutube(" https://youtu.be/abc123 ")
        assertEquals("https://youtu.be/abc123", repo.youtube)
    }

    // ---- Dashboard ----

    @Test
    fun `continue goes to the first unfinished step in dashboard order`() {
        val repo = FakeSetupRepository(
            steps = mapOf("PROFILE" to true, "BANK_ACCOUNT" to true, "EMERGENCY_CONTACT" to false,
                "YOUTUBE_CHANNEL" to true, "IDENTIFICATION" to false),
        )
        val vm = SetupDashboardViewModel(repo)

        vm.load()

        assertEquals(SetupDashboardViewModel.EMERGENCY, vm.nextStep())
    }

    @Test
    fun `continue has nowhere to go when all steps are done`() {
        val repo = FakeSetupRepository(steps = SetupDashboardViewModel.STEP_ORDER.associateWith { true })
        val vm = SetupDashboardViewModel(repo)

        vm.load()

        assertNull(vm.nextStep())
    }
}
