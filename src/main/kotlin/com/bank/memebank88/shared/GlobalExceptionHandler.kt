package com.bank.memebank88.shared

import com.bank.memebank88.shared.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Safety net for genuinely unexpected exceptions (e.g. a JDBC failure). Expected domain failures
 * are values mapped to responses in [toResponse] and never reach here. Logs a concise one-line
 * warning — no stack-trace dump — and returns the standard [ErrorResponse] body.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handle(ex: Exception): ResponseEntity<ErrorResponse> {
        log.warn("Unhandled request error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(500, "Internal Server Error", "INTERNAL_ERROR", "An unexpected error occurred."))
    }
}
