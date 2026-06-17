package com.bank.memebank88.onboarding.adapter.inbound.web

import com.bank.memebank88.onboarding.domain.OnboardingError
import com.bank.memebank88.shared.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Maps an [OnboardingError] (a value, never thrown) to an HTTP response with a proper status code
 * and the shared [ErrorResponse] body. Lives in the onboarding web adapter so the domain stays free
 * of HTTP concerns.
 */
fun OnboardingError.toResponse(): ResponseEntity<ErrorResponse> {
    val (status, code, message) = when (this) {
        is OnboardingError.OnboardingNotFound ->
            Triple(HttpStatus.NOT_FOUND, "ONBOARDING_NOT_FOUND", "Onboarding $onboardingId was not found.")
        is OnboardingError.OnboardingStepOutOfOrder ->
            Triple(HttpStatus.CONFLICT, "STEP_OUT_OF_ORDER", "Onboarding step expected status $expected but was $actual.")
        is OnboardingError.VerificationFailed ->
            Triple(HttpStatus.UNPROCESSABLE_ENTITY, "VERIFICATION_FAILED", "The $kind verification code did not match.")
        is OnboardingError.InvalidCustomerInfo ->
            Triple(HttpStatus.BAD_REQUEST, "INVALID_CUSTOMER_INFO", reason)
        is OnboardingError.InvalidCredentials ->
            Triple(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password.")
    }
    return ResponseEntity.status(status).body(ErrorResponse(status.value(), status.reasonPhrase, code, message))
}
