package com.ebookfrenzy.duvproject.ui.auth

import com.ebookfrenzy.duvproject.MainDispatcherRule
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.ApiResult
import com.ebookfrenzy.duvproject.data.repository.AccountCategory
import com.ebookfrenzy.duvproject.ui.auth.signup.SignUpEvent
import com.ebookfrenzy.duvproject.ui.auth.signup.SignUpViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private fun SignUpViewModel.submit(phone: String = "0803 123 4567") = onSignUpClicked(
        firstName = "Ada", lastName = "Obi", email = "ada@duv.ng", phone = phone,
        password = "secret1", confirmPassword = "secret1",
    )

    @Test
    fun `category must be chosen`() {
        val repo = FakeAuthRepository()
        val vm = SignUpViewModel(repo)

        vm.submit()

        assertEquals(R.string.please_choose_a_category, vm.uiState.value.categoryError)
        assertNull(repo.lastRegisterCategory)
    }

    @Test
    fun `user category is sent and phone spaces are removed`() = runTest {
        val repo = FakeAuthRepository()
        val vm = SignUpViewModel(repo)

        vm.onCategoryChosen(AccountCategory.USER)
        vm.submit()

        assertEquals(AccountCategory.USER, repo.lastRegisterCategory)
        assertEquals("08031234567", repo.lastRegisterPhone)
        assertEquals(SignUpEvent.ShowAccountCreated(isEntertainer = false), vm.events.first())
    }

    @Test
    fun `chosen entertainer type is sent`() = runTest {
        val repo = FakeAuthRepository()
        val vm = SignUpViewModel(repo)

        vm.onCategoryChosen(AccountCategory.DJ)
        vm.submit()

        assertEquals(AccountCategory.DJ, repo.lastRegisterCategory)
    }

    @Test
    fun `missing names block sign up`() {
        val repo = FakeAuthRepository()
        val vm = SignUpViewModel(repo)

        vm.onSignUpClicked("", " ", "ada@duv.ng", "08031234567", "secret1", "secret1")

        assertEquals(R.string.first_name_cannot_be_empty, vm.uiState.value.firstNameError)
        assertEquals(R.string.last_name_cannot_be_empty, vm.uiState.value.lastNameError)
        assertNull(repo.lastRegisterCategory)
    }

    @Test
    fun `server field errors are kept for the form`() = runTest {
        val repo = FakeAuthRepository(
            ApiResult.Failure("Email already registered", fieldErrors = mapOf("email" to "Email already registered")),
        )
        val vm = SignUpViewModel(repo)

        vm.onCategoryChosen(AccountCategory.USER)
        vm.submit()

        assertEquals(SignUpEvent.ShowMessage("Email already registered"), vm.events.first())
        assertEquals("Email already registered", vm.uiState.value.serverFieldErrors["email"])
    }
}
