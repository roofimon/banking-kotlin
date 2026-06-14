package com.bank.djackatron2.adapter.inbound.web

import com.bank.djackatron2.adapter.inbound.web.dto.ErrorResponse
import com.bank.djackatron2.domain.DomainError
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
    }
    return ResponseEntity.status(status).body(ErrorResponse(status.value(), status.reasonPhrase, code, message))
}
