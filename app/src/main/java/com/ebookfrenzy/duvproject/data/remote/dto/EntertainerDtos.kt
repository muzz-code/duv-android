package com.ebookfrenzy.duvproject.data.remote.dto

/**
 * Entertainer set-up, as exchanged with the backend's MyEntertainerController (/api/v1/entertainers/me).
 * Enum values are sent as the backend's names (e.g. "LIVE_BAND", "WEDDING", "NATIONAL_ID").
 */
data class EntertainerSetupDto(
    val id: Long,
    val stageName: String?,
    val entertainerType: String?,
    val yearStarted: Int?,
    val baseCharge: Double?,
    val highestCharge: Double?,
    val state: String?,
    val cityLga: String?,
    val travelsToOtherStates: Boolean,
    val availableFor: List<String>?,
    val alternatePhone: String?,
    val about: String?,
    /** Relative path of the photo endpoint, or null when no photo has been uploaded. */
    val photoUrl: String?,
    val youtubeUrl: String?,
    val bankAccount: BankAccountDto?,
    val nextOfKin: ContactDto?,
    val professionalContact: ContactDto?,
    val identification: IdentificationDto?,
    /** INCOMPLETE, PENDING_APPROVAL, APPROVED or REJECTED. */
    val status: String,
    val rejectionReason: String?,
    val setupProgress: SetupProgressDto,
)

data class SetupProgressDto(
    val completedSteps: Int,
    val totalSteps: Int,
    /** PROFILE, IDENTIFICATION, BANK_ACCOUNT, EMERGENCY_CONTACT, YOUTUBE_CHANNEL → done? */
    val steps: Map<String, Boolean>,
)

data class ProfileDetailsRequest(
    val stageName: String,
    val entertainerType: String,
    val yearStarted: Int,
    val baseCharge: Long,
    val highestCharge: Long,
    val state: String,
    val cityLga: String,
    val travelsToOtherStates: Boolean,
    val availableFor: List<String>,
    val alternatePhone: String? = null,
    val about: String? = null,
)

/** A saved payout account. [accountName] is the bank's name for it; [bankCode] is null for accounts saved before
 * bank verification existed. */
data class BankAccountDto(
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val bankCode: String? = null,
)

/** Saving only sends the bank and number: the server asks the bank for the holder's name. */
data class SaveBankAccountRequest(val bankCode: String, val accountNumber: String)

/** A bank from GET /api/v1/banks ([code] is the verification provider's bank code). */
data class BankDto(val code: String, val name: String)

data class ResolveAccountRequest(val bankCode: String, val accountNumber: String)

/** The bank's answer: [accountName] is the holder's name as the bank has it. */
data class ResolvedAccountDto(val bankCode: String, val bankName: String, val accountNumber: String, val accountName: String)

data class YoutubeRequest(val youtubeUrl: String)

/** Dates are ISO yyyy-MM-dd; expiryDate is null for IDs that don't expire. */
data class IdentificationDto(
    val documentType: String,
    val idNumber: String,
    val issueDate: String,
    val expiryDate: String?,
)

data class ContactDto(
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String,
    val relationship: String,
)

data class EmergencyContactRequest(val nextOfKin: ContactDto, val professionalContact: ContactDto?)
