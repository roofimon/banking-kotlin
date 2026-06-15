package com.bank.djackatron2.application.event

import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.outbound.ReceiptSenderPort
import com.bank.djackatron2.port.outbound.TransferReceiptRepositoryPort
import org.junit.jupiter.api.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TransferReceiptWorkerTest {

    @Test
    fun persistsThenForwardsToAllSenders() {
        val repository = mock(TransferReceiptRepositoryPort::class.java)
        val senderA = mock(ReceiptSenderPort::class.java)
        val senderB = mock(ReceiptSenderPort::class.java)
        val worker = TransferReceiptWorker(repository, listOf(senderA, senderB))
        val receipt = TransferReceipt("t-1", Account("A123", 100.00), Account("C456", 0.00))

        worker.on(TransferCompletedEvent(receipt))

        // Saved before being dispatched.
        inOrder(repository, senderA).apply {
            verify(repository).save(receipt)
            verify(senderA).send(receipt)
        }
        verify(senderB).send(receipt)
    }
}
