package com.bank.djackatron2.application.event

import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.outbound.ReceiptSenderPort
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TransferReceiptWorkerTest {

    @Test
    fun forwardsReceiptToSender() {
        val sender = mock(ReceiptSenderPort::class.java)
        val worker = TransferReceiptWorker(sender)
        val receipt = TransferReceipt(Account("A123", 100.00), Account("C456", 0.00))

        worker.on(TransferCompletedEvent(receipt))

        verify(sender).send(receipt)
    }
}
