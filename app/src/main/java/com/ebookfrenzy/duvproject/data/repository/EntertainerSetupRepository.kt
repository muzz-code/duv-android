package com.ebookfrenzy.duvproject.data.repository

import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.remote.EntertainerApi
import com.ebookfrenzy.duvproject.data.remote.apiCall
import com.ebookfrenzy.duvproject.data.remote.dto.BankDto
import com.ebookfrenzy.duvproject.data.remote.dto.ContactDto
import com.ebookfrenzy.duvproject.data.remote.dto.EmergencyContactRequest
import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.data.remote.dto.IdentificationDto
import com.ebookfrenzy.duvproject.data.remote.dto.ProfileDetailsRequest
import com.ebookfrenzy.duvproject.data.remote.dto.ResolveAccountRequest
import com.ebookfrenzy.duvproject.data.remote.dto.ResolvedAccountDto
import com.ebookfrenzy.duvproject.data.remote.dto.SaveBankAccountRequest
import com.ebookfrenzy.duvproject.data.remote.dto.YoutubeRequest
import com.google.gson.Gson
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** The entertainer's five set-up steps. Every successful call returns the updated profile and progress. */
interface EntertainerSetupRepository {
    suspend fun load(): ApiResult<EntertainerSetupDto>
    suspend fun saveProfile(request: ProfileDetailsRequest): ApiResult<EntertainerSetupDto>
    suspend fun uploadPhoto(jpeg: ByteArray): ApiResult<EntertainerSetupDto>
    suspend fun downloadPhoto(path: String): ApiResult<ByteArray>
    suspend fun saveIdentification(details: IdentificationDto): ApiResult<EntertainerSetupDto>
    suspend fun saveBankAccount(bankCode: String, accountNumber: String): ApiResult<EntertainerSetupDto>
    suspend fun banks(): ApiResult<List<BankDto>>
    suspend fun resolveAccount(bankCode: String, accountNumber: String): ApiResult<ResolvedAccountDto>
    suspend fun saveEmergencyContacts(nextOfKin: ContactDto, professional: ContactDto): ApiResult<EntertainerSetupDto>
    suspend fun saveYoutube(url: String): ApiResult<EntertainerSetupDto>
}

class NetworkEntertainerSetupRepository @Inject constructor(
    private val api: EntertainerApi,
    private val gson: Gson,
) : EntertainerSetupRepository {

    override suspend fun load() = apiCall(gson) { api.mine() }

    override suspend fun saveProfile(request: ProfileDetailsRequest) = apiCall(gson) { api.updateProfile(request) }

    override suspend fun uploadPhoto(jpeg: ByteArray) = apiCall(gson) {
        val body = jpeg.toRequestBody("image/jpeg".toMediaType())
        api.uploadPhoto(MultipartBody.Part.createFormData("file", "profile.jpg", body))
    }

    override suspend fun downloadPhoto(path: String) = apiCall(gson) { api.download(path).use { it.bytes() } }

    override suspend fun saveIdentification(details: IdentificationDto) =
        apiCall(gson) { api.updateIdentification(details) }

    override suspend fun saveBankAccount(bankCode: String, accountNumber: String) =
        apiCall(gson) { api.updateBankAccount(SaveBankAccountRequest(bankCode, accountNumber)) }

    override suspend fun banks() = apiCall(gson) { api.banks() }

    override suspend fun resolveAccount(bankCode: String, accountNumber: String) =
        apiCall(gson) { api.resolveAccount(ResolveAccountRequest(bankCode, accountNumber)) }

    override suspend fun saveEmergencyContacts(nextOfKin: ContactDto, professional: ContactDto) =
        apiCall(gson) { api.updateEmergencyContact(EmergencyContactRequest(nextOfKin, professional)) }

    override suspend fun saveYoutube(url: String) = apiCall(gson) { api.updateYoutube(YoutubeRequest(url)) }
}
