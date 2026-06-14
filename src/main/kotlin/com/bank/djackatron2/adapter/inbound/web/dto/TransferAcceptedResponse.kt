package com.bank.djackatron2.adapter.inbound.web.dto

/**
 * Body returned (202 Accepted) when a transfer is accepted for async processing. The receipt is
 * delivered out-of-band by the worker, not in this response.
 */
data class TransferAcceptedResponse(val message: String, val status: String = "ACCEPTED")
