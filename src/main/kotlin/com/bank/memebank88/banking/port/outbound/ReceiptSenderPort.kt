package com.bank.memebank88.banking.port.outbound

import com.bank.memebank88.banking.domain.TransferReceipt

/**
 * Outbound port for delivering a completed transfer's receipt to the outside world
 * (logging, email, message queue, …). The default adapter logs it.
 */
interface ReceiptSenderPort {
    fun send(receipt: TransferReceipt)
}
