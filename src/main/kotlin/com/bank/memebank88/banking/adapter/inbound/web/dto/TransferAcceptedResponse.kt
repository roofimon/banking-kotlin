package com.bank.memebank88.banking.adapter.inbound.web.dto

/**
 * Body returned (202 Accepted) when a transfer is accepted for async processing. Carries the
 * [transferId] tracking handle; the receipt itself is delivered out-of-band by the worker.
 */
data class TransferAcceptedResponse(
    val transferId: String,
    val message: String,
    val status: String = "ACCEPTED",
)
