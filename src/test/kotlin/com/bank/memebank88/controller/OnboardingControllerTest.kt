package com.bank.memebank88.adapter.inbound.web

import arrow.core.left
import arrow.core.right
import com.bank.memebank88.adapter.inbound.web.dto.CustomerInfoRequest
import com.bank.memebank88.adapter.inbound.web.dto.ErrorResponse
import com.bank.memebank88.adapter.inbound.web.dto.OnboardingResponse
import com.bank.memebank88.adapter.inbound.web.dto.ScoreRequest
import com.bank.memebank88.adapter.inbound.web.dto.StartOnboardingRequest
import com.bank.memebank88.adapter.inbound.web.dto.VerifyCodeRequest
import com.bank.memebank88.domain.DomainError
import com.bank.memebank88.domain.Onboarding
import com.bank.memebank88.domain.OnboardingStatus
import com.bank.memebank88.port.inbound.OnboardingUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.time.Instant

class OnboardingControllerTest {

    private val onboarding: OnboardingUseCase = mock(OnboardingUseCase::class.java)
    private val controller = OnboardingController(onboarding)

    private fun onboardingAt(
        status: OnboardingStatus,
        accountId: String? = null,
        score: Int? = null,
        password: String? = null,
    ) =
        Onboarding(
            id = "ob-1",
            email = "jane@example.com",
            status = status,
            emailCode = "123456",
            name = "Jane",
            phone = "0812345678",
            sessionToken = "tok",
            creditScore = score,
            accountId = accountId,
            password = password,
            createdAt = Instant.now(),
        )

    @Test
    fun startReturns201() {
        `when`(onboarding.start("jane@example.com"))
            .thenReturn(onboardingAt(OnboardingStatus.STARTED).right())

        val result = controller.start(StartOnboardingRequest("jane@example.com"))

        assertEquals(HttpStatus.CREATED, result.statusCode)
        val body = result.body as OnboardingResponse
        assertEquals("STARTED", body.status)
        assertEquals("123456", body.emailCode)
    }

    @Test
    fun verifyEmailReturns200() {
        `when`(onboarding.verifyEmail("ob-1", "123456"))
            .thenReturn(onboardingAt(OnboardingStatus.EMAIL_VERIFIED).right())

        val result = controller.verifyEmail("ob-1", VerifyCodeRequest("123456"))

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("EMAIL_VERIFIED", (result.body as OnboardingResponse).status)
    }

    @Test
    fun scoreCompletedReturnsAccountId() {
        `when`(onboarding.score("ob-1", 50000.0, "SALARIED", 2000.0, 100000.0))
            .thenReturn(onboardingAt(OnboardingStatus.COMPLETED, accountId = "AC0000001", score = 720, password = "Ab3Cd").right())

        val result = controller.score("ob-1", ScoreRequest(50000.0, "SALARIED", 2000.0, 100000.0))

        assertEquals(HttpStatus.OK, result.statusCode)
        val body = result.body as OnboardingResponse
        assertEquals("COMPLETED", body.status)
        assertEquals("AC0000001", body.accountId)
        assertEquals(720, body.creditScore)
        assertEquals("Ab3Cd", body.password)
    }

    @Test
    fun verificationFailureReturns422() {
        `when`(onboarding.verifyEmail("ob-1", "000000"))
            .thenReturn(DomainError.VerificationFailed("email").left())

        val result = controller.verifyEmail("ob-1", VerifyCodeRequest("000000"))

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
        assertEquals("VERIFICATION_FAILED", (result.body as ErrorResponse).code)
    }

    @Test
    fun stepOutOfOrderReturns409() {
        `when`(onboarding.submitInfo("ob-1", "Jane", "0812345678"))
            .thenReturn(DomainError.OnboardingStepOutOfOrder("EMAIL_VERIFIED", "STARTED").left())

        val result = controller.submitInfo("ob-1", CustomerInfoRequest("Jane", "0812345678"))

        assertEquals(HttpStatus.CONFLICT, result.statusCode)
        assertEquals("STEP_OUT_OF_ORDER", (result.body as ErrorResponse).code)
    }

    @Test
    fun unknownOnboardingReturns404() {
        `when`(onboarding.find("nope")).thenReturn(DomainError.OnboardingNotFound("nope").left())

        val result = controller.find("nope")

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        assertEquals("ONBOARDING_NOT_FOUND", (result.body as ErrorResponse).code)
    }
}
