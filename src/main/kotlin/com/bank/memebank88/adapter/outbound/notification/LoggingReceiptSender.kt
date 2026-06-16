package com.bank.memebank88.adapter.outbound.notification

import com.bank.memebank88.domain.TransferReceipt
import com.bank.memebank88.port.outbound.ReceiptSenderPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Default [ReceiptSenderPort] adapter: logs the receipt. Swap for an email/SMS/queue adapter later.
 */
@Component
class LoggingReceiptSender : ReceiptSenderPort {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(receipt: TransferReceipt) {
        log.info("Transfer receipt dispatched:\n{}", receipt)
    }
}
