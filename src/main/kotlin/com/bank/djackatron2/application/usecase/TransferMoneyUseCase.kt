package com.bank.djackatron2.application.usecase

import com.bank.djackatron2.application.exception.OutOfServiceException
import com.bank.djackatron2.domain.InsufficientFundsException
import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.port.outbound.EventStorePort
import com.bank.djackatron2.port.outbound.FeePolicyPort
import com.bank.djackatron2.port.outbound.TimeServicePort
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class TransferMoneyUseCase(
    private val accountRepository: AccountRepositoryPort,
    private val feePolicy: FeePolicyPort,
    private val eventStore: EventStorePort,
) : TransferUseCase {

    private var minimumTransferAmount = 1.00
    private var timeService: TimeServicePort? = null

    override fun transfer(amount: Double, srcAcctId: String, dstAcctId: String): TransferReceipt {
        if (amount < minimumTransferAmount) {
            throw IllegalArgumentException("transfer amount must be at least $minimumTransferAmount")
        }

        if (timeService != null && !timeService!!.isServiceAvailable(LocalTime.now())) {
            throw OutOfServiceException()
        }

        val srcAcct = accountRepository.findById(srcAcctId)
        val dstAcct = accountRepository.findById(dstAcctId)

        val receipt = TransferReceipt(
            initialSourceAccountCopy = srcAcct,
            initialDestinationAccountCopy = dstAcct,
        )

        val fee = feePolicy.calculateFee(amount)
        if (fee > 0) {
            try {
                srcAcct.debit(fee)
            } catch (e: InsufficientFundsException) {
                e.printStackTrace()
            }
        }

        receipt.setTransferAmount(amount)
        receipt.setFeeAmount(fee)

        try {
            srcAcct.debit(amount)
        } catch (e: InsufficientFundsException) {
            throw InsufficientFundsException(srcAcct, amount)
        }

        dstAcct.credit(amount)

        receipt.setFinalSourceAccount(srcAcct)
        receipt.setFinalDestinationAccount(dstAcct)

        (srcAcct.domainEvents() + dstAcct.domainEvents()).forEach { eventStore.append(it) }
        srcAcct.clearDomainEvents()
        dstAcct.clearDomainEvents()

        return receipt
    }

    override fun setMinimumTransferAmount(minimumTransferAmount: Double) {
        this.minimumTransferAmount = minimumTransferAmount
    }

    override fun setTimeService(timeService: TimeServicePort) {
        this.timeService = timeService
    }
}
