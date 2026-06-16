package com.bank.memebank88.adapter.inbound.web

import com.bank.memebank88.adapter.inbound.web.dto.ErrorResponse
import com.bank.memebank88.domain.DomainError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Maps a [DomainError] (a value, never thrown) to an HTTP response with a proper status code and
 * the standard [ErrorResponse] body. Lives in the web adapter so the domain stays free of HTTP
 * concerns.
 */
fun DomainError.toResponse(): ResponseEntity<ErrorResponse> {
    val (status, code, message) = when (this) {
        is DomainError.AccountNotFound ->
            Triple(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account $accountId was not found.")
        is DomainError.InvalidAmount ->
            Triple(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount $amount must be greater than zero.")
        is DomainError.BelowMinimum ->
            Triple(HttpStatus.UNPROCESSABLE_ENTITY, "BELOW_MINIMUM", "$operation amount $amount is below the minimum of $minimum.")
        is DomainError.InsufficientFunds ->
            Triple(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS", "Insufficient funds in account $accountId (attempted $attempted, balance $balance).")
        is DomainError.OutOfService ->
            Triple(HttpStatus.SERVICE_UNAVAILABLE, "OUT_OF_SERVICE", "The transfer service is currently outside its operating hours.")
        is DomainError.OnboardingNotFound ->
            Triple(HttpStatus.NOT_FOUND, "ONBOARDING_NOT_FOUND", "Onboarding $onboardingId was not found.")
        is DomainError.OnboardingStepOutOfOrder ->
            Triple(HttpStatus.CONFLICT, "STEP_OUT_OF_ORDER", "Onboarding step expected status $expected but was $actual.")
        is DomainError.VerificationFailed ->
            Triple(HttpStatus.UNPROCESSABLE_ENTITY, "VERIFICATION_FAILED", "The $kind verification code did not match.")
        is DomainError.InvalidCustomerInfo ->
            Triple(HttpStatus.BAD_REQUEST, "INVALID_CUSTOMER_INFO", reason)
        is DomainError.InvalidCredentials ->
            Triple(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password.")
    }
    return ResponseEntity.status(status).body(ErrorResponse(status.value(), status.reasonPhrase, code, message))
}
