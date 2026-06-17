package com.bank.memebank88.banking.port.inbound

/** Inputs for a money transfer, grouped into one value (see [TransferUseCase.transfer]). */
data class TransferCommand(
    val amount: Double,
    val srcAcctId: String,
    val dstAcctId: String,
)
