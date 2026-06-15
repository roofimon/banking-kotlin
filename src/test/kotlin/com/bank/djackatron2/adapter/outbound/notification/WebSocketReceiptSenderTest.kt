package com.bank.djackatron2.adapter.outbound.notification

import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.TransferReceipt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.messaging.simp.SimpMessagingTemplate

class WebSocketReceiptSenderTest {

    @Test
    fun pushesReceiptToBothAccountTopics() {
        val messagingTemplate = mock(SimpMessagingTemplate::class.java)
        val sender = WebSocketReceiptSender(messagingTemplate)

        val receipt = TransferReceipt("t-1", Account("A123", 100.00), Account("C456", 0.00)).apply {
            setFinalSourceAccount(Account("A123", 45.00))
            setFinalDestinationAccount(Account("C456", 50.00))
        }

        sender.send(receipt)

        verify(messagingTemplate).convertAndSend("/topic/receipts/A123", receipt)
        verify(messagingTemplate).convertAndSend("/topic/receipts/C456", receipt)
    }
}
