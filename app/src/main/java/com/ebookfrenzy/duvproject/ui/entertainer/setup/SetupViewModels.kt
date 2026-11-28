package com.ebookfrenzy.duvproject.ui.entertainer.setup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.remote.dto.BankDto
import com.ebookfrenzy.duvproject.data.remote.dto.ContactDto
import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.data.remote.dto.IdentificationDto
import com.ebookfrenzy.duvproject.data.remote.dto.ProfileDetailsRequest
import com.ebookfrenzy.duvproject.data.repository.EntertainerSetupRepository
import com.ebookfrenzy.duvproject.util.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** One-off results of a set-up action. */
sealed interface SetupEvent {
    /** Saved on the server; [message] is a string resource to show. */
    data class Saved(@StringRes val message: Int) : SetupEvent

    /** The server (or network) refused; [fieldErrors] maps request field names to messages. */
    data class ServerError(val message: String, val fieldErrors: Map<String, String>) : SetupEvent

    /** Rejected before sending, e.g. an invalid phone number. */
    data class Invalid(@StringRes val message: Int) : SetupEvent
}

/** Shared loading, pre-fill and save logic for every set-up step. */
abstract class SetupStepViewModel(protected val repository: EntertainerSetupRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** The latest profile from the server, used to pre-fill forms and show progress. */
    private val _setup = MutableStateFlow<EntertainerSetupDto?>(null)
    val setup: StateFlow<EntertainerSetupDto?> = _setup.asStateFlow()

    private val _events = Channel<SetupEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Set once the form has been filled from [setup], so rotation doesn't overwrite what the user typed. */
    var prefilled = false

    /** Fetches the profile; with [force] = false it only loads once. */
    fun load(force: Boolean = false) {
        if (!force && _setup.value != null) return
        run(null) { repository.load() }
    }

    protected fun save(@StringRes savedMessage: Int, call: suspend () -> ApiResult<EntertainerSetupDto>) =
        run(savedMessage, call)

    protected fun reject(@StringRes message: Int) {
        viewModelScope.launch { _events.send(SetupEvent.Invalid(message)) }
    }

    protected open fun onLoaded(setup: EntertainerSetupDto) {}

    private fun run(@StringRes savedMessage: Int?, call: suspend () -> ApiResult<EntertainerSetupDto>) {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = call()) {
                is ApiResult.Success -> {
                    _setup.value = result.data
                    onLoaded(result.data)
                    savedMessage?.let { _events.send(SetupEvent.Saved(it)) }
                }
                is ApiResult.Failure -> _events.send(SetupEvent.ServerError(result.message, result.fieldErrors))
            }
            _isLoading.value = false
        }
    }
}

/** Dashboard checklist: progress, and where "Continue Setup" goes. */
@HiltViewModel
class SetupDashboardViewModel @Inject constructor(repository: EntertainerSetupRepository) :
    SetupStepViewModel(repository) {

    /** The first unfinished step in the order the dashboard lists them, or null when all five are done. */
    fun nextStep(): String? {
        val steps = setup.value?.setupProgress?.steps ?: return STEP_ORDER.first()
        return STEP_ORDER.firstOrNull { steps[it] != true }
    }

    companion object {
        const val PROFILE = "PROFILE"
        const val BANK = "BANK_ACCOUNT"
        const val EMERGENCY = "EMERGENCY_CONTACT"
        const val YOUTUBE = "YOUTUBE_CHANNEL"
        const val IDENTIFICATION = "IDENTIFICATION"
        val STEP_ORDER = listOf(PROFILE, BANK, EMERGENCY, YOUTUBE, IDENTIFICATION)
    }
}

/** What the Entertainer Profile form holds, as shown on screen (labels, not backend values). */
data class ProfileForm(
    val stageName: String,
    val type: String,
    val state: String,
    val lga: String,
    val baseCharge: String,
    val highestCharge: String,
    val travel: String,
    val year: String,
    val availableFor: String,
    val about: String,
)

/** Set-up step 1, plus the profile photo. */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(repository: EntertainerSetupRepository) :
    SetupStepViewModel(repository) {

    /** The current photo's bytes (downloaded or just uploaded), for the circle. */
    private val _photo = MutableStateFlow<ByteArray?>(null)
    val photo: StateFlow<ByteArray?> = _photo.asStateFlow()

    /** The photo URL whose bytes are in [photo], so it isn't downloaded again after every save. */
    private var shownPhotoPath: String? = null

    override fun onLoaded(setup: EntertainerSetupDto) {
        val path = setup.photoUrl ?: return
        if (path == shownPhotoPath) return
        shownPhotoPath = path
        viewModelScope.launch {
            (repository.downloadPhoto(path) as? ApiResult.Success)?.let { _photo.value = it.data }
        }
    }

    /** Maps the screen's labels to the backend request, or returns null after reporting what's wrong. */
    fun toRequest(form: ProfileForm): ProfileDetailsRequest? {
        val base = SetupOptions.chargeFrom(form.baseCharge)
        val highest = SetupOptions.chargeFrom(form.highestCharge)
        if (base == null || highest == null) return null.also { reject(R.string.setup_error_charges) }
        if (base > highest) return null.also { reject(R.string.setup_error_charge_range) }
        val type = SetupOptions.entertainmentTypes.valueFor(form.type)
            ?: return null.also { reject(R.string.setup_error_type) }
        val year = form.year.toIntOrNull() ?: return null.also { reject(R.string.setup_error_year) }
        val events = form.availableFor.split(SetupOptions.SEPARATOR).mapNotNull { SetupOptions.eventTypes.valueFor(it) }
        if (events.isEmpty()) return null.also { reject(R.string.setup_error_available_for) }
        return ProfileDetailsRequest(
            stageName = form.stageName.trim(),
            entertainerType = type,
            yearStarted = year,
            baseCharge = base,
            highestCharge = highest,
            state = form.state,
            cityLga = form.lga,
            travelsToOtherStates = form.travel == SetupOptions.YES,
            availableFor = events,
            alternatePhone = setup.value?.alternatePhone,
            about = form.about.trim().ifBlank { null },
        )
    }

    fun saveProfile(form: ProfileForm) {
        val request = toRequest(form) ?: return
        save(R.string.setup_saved_profile) { repository.saveProfile(request) }
    }

    /** [jpeg] is the picked photo, already scaled down and compressed by the screen. */
    fun uploadPhoto(jpeg: ByteArray) {
        _photo.value = jpeg
        save(R.string.setup_saved_photo) { repository.uploadPhoto(jpeg) }
    }
}

/** One contact as typed on the Emergency Contact screen. */
data class ContactForm(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val relationship: String,
) {
    val isBlank get() = listOf(firstName, lastName, email, phone, relationship).all { it.isBlank() }

    fun toDto() = ContactDto(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        email = email.trim().ifBlank { null },
        phone = phone.filterNot { it.isWhitespace() || it == '-' },
        relationship = relationship,
    )
}

/** Set-up step 4. */
@HiltViewModel
class EmergencyContactViewModel @Inject constructor(repository: EntertainerSetupRepository) :
    SetupStepViewModel(repository) {

    /** Next of kin is required; the professional contact is optional but, once started, must be complete. */
    fun saveContacts(nextOfKin: ContactForm, professional: ContactForm) {
        val kin = nextOfKin.toDto()
        if (!valid(kin)) return
        val pro = if (professional.isBlank) null else professional.toDto()
        if (pro != null) {
            if (listOf(pro.firstName, pro.lastName, pro.phone, pro.relationship).any { it.isBlank() }) {
                return reject(R.string.setup_error_professional_incomplete)
            }
            if (!valid(pro)) return
        }
        save(R.string.setup_saved_contacts) { repository.saveEmergencyContacts(kin, pro) }
    }

    private fun valid(contact: ContactDto): Boolean = when {
        !Validator.mobileValidate(contact.phone) -> false.also { reject(R.string.invalid_phone_number) }
        contact.email != null && !Validator.emailValidate(contact.email) -> false.also { reject(R.string.invalid_email) }
        else -> true
    }
}

/** Where the bank-account lookup stands, for the Bank Account Details card. */
sealed interface AccountLookup {
    /** Not enough to look up yet (no bank chosen, or fewer than 10 digits). */
    data object Idle : AccountLookup
    data object Checking : AccountLookup
    /** The bank knows this account; [accountName] is the holder's name as the bank has it. */
    data class Found(val bankCode: String, val accountNumber: String, val accountName: String) : AccountLookup
    /** The bank doesn't know the account, or the lookup couldn't be done; [message] is ready to show. */
    data class Failed(val message: String) : AccountLookup
}

/** Set-up steps 2, 3 and 5 (the pop-up cards). */
@HiltViewModel
class SetupCardViewModel @Inject constructor(repository: EntertainerSetupRepository) :
    SetupStepViewModel(repository) {

    // ---- Bank account: pick a bank, type the number, and the bank tells us the holder's name ----

    private val _banks = MutableStateFlow<List<BankDto>?>(null)
    /** Banks to pick from; null until loaded. */
    val banks: StateFlow<List<BankDto>?> = _banks.asStateFlow()

    private val _lookup = MutableStateFlow<AccountLookup>(AccountLookup.Idle)
    val lookup: StateFlow<AccountLookup> = _lookup.asStateFlow()

    private var lookupJob: Job? = null
    private var bankInput = ""
    private var numberInput = ""

    fun loadBanks() {
        if (_banks.value != null) return
        viewModelScope.launch {
            when (val result = repository.banks()) {
                is ApiResult.Success -> {
                    _banks.value = result.data
                    lookUp(bankInput, numberInput)   // the form may have been pre-filled before the list arrived
                }
                is ApiResult.Failure -> _lookup.value = AccountLookup.Failed(result.message)
            }
        }
    }

    /** Call whenever the bank or number changes; looks the account up once both are complete. */
    fun lookUp(bankName: String, accountNumber: String) {
        bankInput = bankName
        numberInput = accountNumber
        val bank = bankFor(bankName)
        val digits = accountNumber.filter(Char::isDigit)
        val current = _lookup.value
        if (current is AccountLookup.Found && current.bankCode == bank?.code && current.accountNumber == digits) return
        lookupJob?.cancel()
        if (bank == null || digits.length != ACCOUNT_DIGITS) {
            if (_banks.value != null || current !is AccountLookup.Failed) _lookup.value = AccountLookup.Idle
            return
        }
        _lookup.value = AccountLookup.Checking
        lookupJob = viewModelScope.launch {
            delay(LOOKUP_DEBOUNCE_MS)   // don't call the bank for every keystroke while the user corrects a digit
            _lookup.value = when (val result = repository.resolveAccount(bank.code, digits)) {
                is ApiResult.Success -> AccountLookup.Found(bank.code, digits, result.data.accountName)
                is ApiResult.Failure -> AccountLookup.Failed(result.message)
            }
        }
    }

    /** Saves only an account the bank has confirmed; the server checks it with the bank again. */
    fun saveBank(bankName: String, accountNumber: String) {
        val digits = accountNumber.filter(Char::isDigit)
        if (digits.length != ACCOUNT_DIGITS) return reject(R.string.setup_error_account_number)
        val bank = bankFor(bankName) ?: return reject(R.string.setup_error_bank)
        val found = _lookup.value as? AccountLookup.Found
        if (found == null || found.bankCode != bank.code || found.accountNumber != digits) {
            return reject(R.string.setup_error_account_not_verified)
        }
        save(R.string.setup_saved_bank) { repository.saveBankAccount(bank.code, digits) }
    }

    private fun bankFor(name: String) = _banks.value?.firstOrNull { it.name == name.trim() }

    fun saveYoutube(url: String) {
        val trimmed = url.trim()
        if (!YOUTUBE.matches(trimmed)) return reject(R.string.setup_error_youtube)
        save(R.string.setup_saved_youtube) { repository.saveYoutube(trimmed) }
    }

    fun saveIdentification(type: String, number: String, issueDate: String, expiryDate: String) {
        val documentType = SetupOptions.idTypes.valueFor(type) ?: return reject(R.string.setup_error_id_type)
        val issued = SetupOptions.parseUiDate(issueDate) ?: return reject(R.string.setup_error_issue_date)
        val expires = if (expiryDate.isBlank()) null
        else SetupOptions.parseUiDate(expiryDate) ?: return reject(R.string.setup_error_expiry_date)
        when {
            issued.isAfter(LocalDate.now()) -> return reject(R.string.setup_error_issue_future)
            expires != null && !expires.isAfter(issued) -> return reject(R.string.setup_error_expiry_before_issue)
            expires != null && expires.isBefore(LocalDate.now()) -> return reject(R.string.setup_error_expired)
        }
        val details = IdentificationDto(documentType, number.trim().uppercase(), issued.toString(), expires?.toString())
        save(R.string.setup_saved_identification) { repository.saveIdentification(details) }
    }

    private companion object {
        const val ACCOUNT_DIGITS = 10
        const val LOOKUP_DEBOUNCE_MS = 400L

        /** Same rule as the backend. */
        val YOUTUBE = Regex("^https?://(www\\.|m\\.)?(youtube\\.com|youtu\\.be)/\\S+$")
    }
}
