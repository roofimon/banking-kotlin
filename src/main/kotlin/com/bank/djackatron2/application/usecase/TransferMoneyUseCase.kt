package com.bank.djackatron2.application.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.bank.djackatron2.application.event.TransferCompletedEvent
import com.bank.djackatron2.domain.DomainError
import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.port.outbound.EventStorePort
import com.bank.djackatron2.port.outbound.FeePolicyPort
import com.bank.djackatron2.port.outbound.TimeServicePort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class TransferMoneyUseCase(
    private val accountRepository: AccountRepositoryPort,
    private val feePolicy: FeePolicyPort,
    private val eventStore: EventStorePort,
    private val eventPublisher: ApplicationEventPublisher,
) : TransferUseCase {

    private var minimumTransferAmount = 1.00
    private var timeService: TimeServicePort? = null

    override fun transfer(amount: Double, srcAcctId: String, dstAcctId: String): Either<DomainError, Unit> = either {
        ensure(amount >= minimumTransferAmount) {
            DomainError.BelowMinimum(amount, minimumTransferAmount, "transfer")
        }

        ensure(timeService?.isServiceAvailable(LocalTime.now()) != false) { DomainError.OutOfService }

        val srcAcct = accountRepository.findById(srcAcctId)
            .toEither { DomainError.AccountNotFound(srcAcctId) }.bind()
        val dstAcct = accountRepository.findById(dstAcctId)
            .toEither { DomainError.AccountNotFound(dstAcctId) }.bind()

        val receipt = TransferReceipt(
            initialSourceAccountCopy = srcAcct,
            initialDestinationAccountCopy = dstAcct,
        )

        val fee = feePolicy.calculateFee(amount)
        // Fee-debit failure is intentionally ignored — the transfer still proceeds.
        if (fee > 0) srcAcct.debit(fee)

        receipt.setTransferAmount(amount)
        receipt.setFeeAmount(fee)

        srcAcct.debit(amount).bind()
        dstAcct.credit(amount).bind()

        receipt.setFinalSourceAccount(srcAcct)
        receipt.setFinalDestinationAccount(dstAcct)

        (srcAcct.domainEvents() + dstAcct.domainEvents()).forEach { eventStore.append(it) }
        srcAcct.clearDomainEvents()
        dstAcct.clearDomainEvents()

        // Hand the receipt off to the internal bus; a worker delivers it out-of-band.
        eventPublisher.publishEvent(TransferCompletedEvent(receipt))
    }

    override fun setMinimumTransferAmount(minimumTransferAmount: Double) {
        this.minimumTransferAmount = minimumTransferAmount
    }

    override fun setTimeService(timeService: TimeServicePort) {
        this.timeService = timeService
    }
}
