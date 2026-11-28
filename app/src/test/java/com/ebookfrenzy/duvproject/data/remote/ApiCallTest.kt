package com.ebookfrenzy.duvproject.data.remote

import com.ebookfrenzy.duvproject.data.remote.dto.LoginRequest
import com.ebookfrenzy.duvproject.data.remote.dto.RefreshTokenRequest
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Checks the app reads the backend's real JSON shapes (AuthResponse and ApiError). */
class ApiCallTest {

    private val server = MockWebServer()
    private val gson = Gson()
    private lateinit var api: AuthApi

    @Before
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `parses a login response`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"accessToken":"a","refreshToken":"r","tokenType":"Bearer","expiresIn":900,
                   "user":{"id":1,"firstName":"Ada","lastName":"Obi","email":"ada@duv.ng","phone":"08031234567",
                   "signupCategory":"DJ","roles":["USER","ENTERTAINER"],"emailVerified":false,
                   "createdAt":"2026-11-25T10:00:00Z"}}""",
            ),
        )

        val result = apiCall(gson) { api.login(LoginRequest("ada@duv.ng", "secret1")) }

        val user = (result as ApiResult.Success).data.user
        assertEquals(listOf("USER", "ENTERTAINER"), user.roles)
        assertEquals("/api/v1/auth/login", server.takeRequest().path)
    }

    @Test
    fun `turns the backend error format into a message and field errors`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"timestamp":"2026-11-25T10:00:00Z","status":400,"code":"VALIDATION_FAILED",
                   "message":"Some fields are invalid","path":"/api/v1/auth/register",
                   "fieldErrors":[{"field":"phone","message":"Invalid phone number"}]}""",
            ),
        )

        val result = apiCall(gson) { api.login(LoginRequest("x", "y")) }

        result as ApiResult.Failure
        assertEquals("Some fields are invalid", result.message)
        assertEquals(mapOf("phone" to "Invalid phone number"), result.fieldErrors)
    }

    @Test
    fun `empty 204 reply from logout is a success, not a crash`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = apiCall(gson) { api.logout(RefreshTokenRequest("r")).requireSuccess() }

        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun `error status on a no-body endpoint becomes a Failure`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"message":"Invalid email"}"""))

        val result = apiCall(gson) { api.logout(RefreshTokenRequest("r")).requireSuccess() }

        assertEquals("Invalid email", (result as ApiResult.Failure).message)
    }

    @Test
    fun `no server is reported as a network error`() = runTest {
        server.shutdown()

        val result = apiCall(gson) { api.login(LoginRequest("x", "y")) }

        assertTrue((result as ApiResult.Failure).isNetworkError)
    }
}
