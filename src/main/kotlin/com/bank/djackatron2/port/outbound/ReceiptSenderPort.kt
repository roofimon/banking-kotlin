package com.bank.djackatron2.port.outbound

import com.bank.djackatron2.domain.TransferReceipt

/**
 * Outbound port for delivering a completed transfer's receipt to the outside world
 * (logging, email, message queue, …). The default adapter logs it.
 */
interface ReceiptSenderPort {
    fun send(receipt: TransferReceipt)
}
