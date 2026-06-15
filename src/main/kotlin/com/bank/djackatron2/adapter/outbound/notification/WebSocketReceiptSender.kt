package com.bank.djackatron2.adapter.outbound.notification

import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.outbound.ReceiptSenderPort
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

/**
 * [ReceiptSenderPort] that pushes the receipt to subscribed web clients over STOMP. Sends to both
 * the source and destination account topics so either party can watch.
 */
@Component
class WebSocketReceiptSender(private val messagingTemplate: SimpMessagingTemplate) : ReceiptSenderPort {

    override fun send(receipt: TransferReceipt) {
        val src = receipt.getFinalSourceAccount().getId()
        val dst = receipt.getFinalDestinationAccount().getId()
        messagingTemplate.convertAndSend("/topic/receipts/$src", receipt)
        messagingTemplate.convertAndSend("/topic/receipts/$dst", receipt)
    }
}
