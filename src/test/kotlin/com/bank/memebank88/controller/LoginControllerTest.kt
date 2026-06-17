package com.bank.memebank88.onboarding.adapter.inbound.web

import arrow.core.left
import arrow.core.right
import com.bank.memebank88.shared.ErrorResponse
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.LoginRequest
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.LoginResponse
import com.bank.memebank88.onboarding.domain.Customer
import com.bank.memebank88.onboarding.domain.OnboardingError
import com.bank.memebank88.onboarding.port.inbound.LoginUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.time.Instant

class LoginControllerTest {

    private val login: LoginUseCase = mock(LoginUseCase::class.java)
    private val controller = LoginController(login)

    @Test
    fun successReturnsCustomerIdentity() {
        `when`(login.login("jane@example.com", "Ab3Cd")).thenReturn(
            Customer(
                accountId = "A100",
                email = "jane@example.com",
                name = "Jane Doe",
                phone = "0812345678",
                password = "Ab3Cd",
                creditScore = 720,
                createdAt = Instant.now(),
            ).right(),
        )

        val result = controller.login(LoginRequest("jane@example.com", "Ab3Cd"))

        assertEquals(HttpStatus.OK, result.statusCode)
        val body = result.body as LoginResponse
        assertEquals("A100", body.accountId)
        assertEquals("Jane Doe", body.name)
        assertEquals(720, body.creditScore)
    }

    @Test
    fun invalidCredentialsReturn401() {
        `when`(login.login("jane@example.com", "wrong"))
            .thenReturn(OnboardingError.InvalidCredentials.left())

        val result = controller.login(LoginRequest("jane@example.com", "wrong"))

        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
        val body = result.body as ErrorResponse
        assertEquals("INVALID_CREDENTIALS", body.code)
    }
}
