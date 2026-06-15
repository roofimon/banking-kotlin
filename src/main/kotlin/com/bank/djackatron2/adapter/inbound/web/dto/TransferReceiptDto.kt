package com.bank.djackatron2.adapter.inbound.web.dto

/** A persisted transfer receipt as returned by `GET /account/{id}/receipts`. */
data class TransferReceiptDto(
    val srcAccountId: String,
    val dstAccountId: String,
    val transferAmount: Double,
    val feeAmount: Double,
    val srcFinalBalance: Double,
    val dstFinalBalance: Double,
    val createdAt: String,
)
