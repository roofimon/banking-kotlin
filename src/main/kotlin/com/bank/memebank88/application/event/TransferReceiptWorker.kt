package com.bank.memebank88.application.event

import com.bank.memebank88.port.outbound.ReceiptSenderPort
import com.bank.memebank88.port.outbound.TransferReceiptRepositoryPort
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Worker that consumes [TransferCompletedEvent] off the internal bus, persists the receipt, and
 * dispatches it to every [ReceiptSenderPort] (logging, WebSocket push, …). Runs asynchronously
 * (`@Async`, enabled via `@EnableAsync`) so delivery is decoupled from the request thread.
 */
@Component
class TransferReceiptWorker(
    private val receiptRepository: TransferReceiptRepositoryPort,
    private val receiptSenders: List<ReceiptSenderPort>,
) {

    @Async
    @EventListener
    fun on(event: TransferCompletedEvent) {
        receiptRepository.save(event.receipt)
        receiptSenders.forEach { it.send(event.receipt) }
    }
}
