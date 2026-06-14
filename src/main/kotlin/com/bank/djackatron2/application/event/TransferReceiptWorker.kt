package com.bank.djackatron2.application.event

import com.bank.djackatron2.port.outbound.ReceiptSenderPort
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Worker that consumes [TransferCompletedEvent] off the internal bus and sends the receipt out.
 * Runs asynchronously (`@Async`, enabled via `@EnableAsync`) so receipt delivery is decoupled
 * from the request thread.
 */
@Component
class TransferReceiptWorker(private val receiptSender: ReceiptSenderPort) {

    @Async
    @EventListener
    fun on(event: TransferCompletedEvent) = receiptSender.send(event.receipt)
}
