package com.bank.memebank88.banking.adapter.inbound.web

import com.bank.memebank88.banking.domain.BankingError
import com.bank.memebank88.shared.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Maps a [BankingError] (a value, never thrown) to an HTTP response with a proper status code and
 * the shared [ErrorResponse] body. Lives in the banking web adapter so the domain stays free of
 * HTTP concerns.
 */
fun BankingError.toResponse(): ResponseEntity<ErrorResponse> {
    val (status, code, message) = when (this) {
        is BankingError.AccountNotFound ->
            Triple(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account $accountId was not found.")
        is BankingError.InvalidAmount ->
            Triple(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount $amount must be greater than zero.")
        is BankingError.BelowMinimum ->
            Triple(HttpStatus.UNPROCESSABLE_ENTITY, "BELOW_MINIMUM", "$operation amount $amount is below the minimum of $minimum.")
        is BankingError.InsufficientFunds ->
            Triple(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS", "Insufficient funds in account $accountId (attempted $attempted, balance $balance).")
        is BankingError.OutOfService ->
            Triple(HttpStatus.SERVICE_UNAVAILABLE, "OUT_OF_SERVICE", "The transfer service is currently outside its operating hours.")
    }
    return ResponseEntity.status(status).body(ErrorResponse(status.value(), status.reasonPhrase, code, message))
}
