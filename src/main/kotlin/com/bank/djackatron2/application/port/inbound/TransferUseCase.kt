package com.bank.djackatron2.application.port.inbound

import com.bank.djackatron2.domain.TransferReceipt

interface TransferUseCase {
    fun transfer(amount: Double, srcAcctId: String, dstAcctId: String): TransferReceipt
    fun setMinimumTransferAmount(minimumTransferAmount: Double)
    fun setTimeService(timeService: com.bank.djackatron2.application.port.outbound.TimeServicePort)
}
