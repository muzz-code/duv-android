package com.ebookfrenzy.duvproject.data.remote

import com.ebookfrenzy.duvproject.data.remote.dto.BankDto
import com.ebookfrenzy.duvproject.data.remote.dto.EmergencyContactRequest
import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.data.remote.dto.IdentificationDto
import com.ebookfrenzy.duvproject.data.remote.dto.ProfileDetailsRequest
import com.ebookfrenzy.duvproject.data.remote.dto.ResolveAccountRequest
import com.ebookfrenzy.duvproject.data.remote.dto.ResolvedAccountDto
import com.ebookfrenzy.duvproject.data.remote.dto.SaveBankAccountRequest
import com.ebookfrenzy.duvproject.data.remote.dto.YoutubeRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Url

/** The entertainer's own set-up steps (backend MyEntertainerController). Every call returns the whole profile. */
interface EntertainerApi {

    @GET("api/v1/entertainers/me")
    suspend fun mine(): EntertainerSetupDto

    @PUT("api/v1/entertainers/me/profile")
    suspend fun updateProfile(@Body body: ProfileDetailsRequest): EntertainerSetupDto

    @Multipart
    @POST("api/v1/entertainers/me/photo")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): EntertainerSetupDto

    @PUT("api/v1/entertainers/me/identification")
    suspend fun updateIdentification(@Body body: IdentificationDto): EntertainerSetupDto

    @PUT("api/v1/entertainers/me/bank-account")
    suspend fun updateBankAccount(@Body body: SaveBankAccountRequest): EntertainerSetupDto

    /** Banks to choose from. */
    @GET("api/v1/banks")
    suspend fun banks(): List<BankDto>

    /** Asks the bank for the account holder's name (POST so the number stays out of URLs and logs). */
    @POST("api/v1/banks/resolve")
    suspend fun resolveAccount(@Body body: ResolveAccountRequest): ResolvedAccountDto

    @PUT("api/v1/entertainers/me/emergency-contact")
    suspend fun updateEmergencyContact(@Body body: EmergencyContactRequest): EntertainerSetupDto

    @PUT("api/v1/entertainers/me/youtube")
    suspend fun updateYoutube(@Body body: YoutubeRequest): EntertainerSetupDto

    /** Downloads a photo given the profile's photoUrl (a path such as /api/v1/entertainers/7/photo). */
    @GET
    suspend fun download(@Url path: String): ResponseBody
}
