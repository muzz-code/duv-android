package com.ebookfrenzy.duvproject.ui.auth

import com.ebookfrenzy.duvproject.MainDispatcherRule
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.repository.Session
import com.ebookfrenzy.duvproject.ui.auth.login.LoginEvent
import com.ebookfrenzy.duvproject.ui.auth.login.LoginViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    @Test
    fun `empty fields show errors and do not call the server`() {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)

        vm.onLoginClicked(email = "", password = "")

        assertEquals(R.string.email_cannot_be_empty, vm.uiState.value.emailError)
        assertEquals(R.string.pasword_cannot_be_empty, vm.uiState.value.passwordError)
        assertNull(repo.lastLogin)
    }

    @Test
    fun `successful login opens the entertainer home for entertainers`() = runTest {
        val repo = FakeAuthRepository(ApiResult.Success(Session("Tunde", isEntertainer = true)))
        val vm = LoginViewModel(repo)

        vm.onLoginClicked(email = " tunde@duv.ng ", password = "secret1")

        assertEquals("tunde@duv.ng" to "secret1", repo.lastLogin)
        assertEquals(LoginEvent.NavigateToHome(isEntertainer = true), vm.events.first())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `server error is shown to the user`() = runTest {
        val repo = FakeAuthRepository(ApiResult.Failure("Invalid email or password"))
        val vm = LoginViewModel(repo)

        vm.onLoginClicked(email = "ada@duv.ng", password = "wrongpw")

        assertEquals(LoginEvent.ShowMessage("Invalid email or password"), vm.events.first())
    }
}
