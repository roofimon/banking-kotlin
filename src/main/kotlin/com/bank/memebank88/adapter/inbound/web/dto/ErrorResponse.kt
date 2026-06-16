package com.bank.memebank88.adapter.inbound.web.dto

/**
 * Standard error body returned for every failed request.
 *
 * @property status  HTTP status code (e.g. 404).
 * @property error   HTTP reason phrase (e.g. "Not Found").
 * @property code    Machine-readable error code for clients to switch on (e.g. "ACCOUNT_NOT_FOUND").
 * @property message Human-readable detail.
 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
)
