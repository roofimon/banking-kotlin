package com.bank.memebank88.banking.application.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.bank.memebank88.banking.application.event.TransferCompletedEvent
import com.bank.memebank88.banking.domain.BankingError
import com.bank.memebank88.banking.domain.TransferId
import com.bank.memebank88.banking.domain.TransferReceipt
import com.bank.memebank88.banking.port.inbound.TransferCommand
import com.bank.memebank88.banking.port.inbound.TransferUseCase
import com.bank.memebank88.banking.port.outbound.AccountRepositoryPort
import com.bank.memebank88.banking.port.outbound.EventStorePort
import com.bank.memebank88.banking.port.outbound.FeePolicyPort
import com.bank.memebank88.banking.port.outbound.TimeServicePort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.util.UUID

@Service
class TransferMoneyUseCase(
    private val accountRepository: AccountRepositoryPort,
    private val feePolicy: FeePolicyPort,
    private val eventStore: EventStorePort,
    private val eventPublisher: ApplicationEventPublisher,
) : TransferUseCase {

    private var minimumTransferAmount = 1.00
    private var timeService: TimeServicePort? = null

    override fun transfer(command: TransferCommand): Either<BankingError, TransferId> = either {
        val (amount, srcAcctId, dstAcctId) = command

        ensure(amount >= minimumTransferAmount) {
            BankingError.BelowMinimum(amount, minimumTransferAmount, "transfer")
        }

        ensure(timeService?.isServiceAvailable(LocalTime.now()) != false) { BankingError.OutOfService }

        val srcAcct = accountRepository.findById(srcAcctId)
            .toEither { BankingError.AccountNotFound(srcAcctId) }.bind()
        val dstAcct = accountRepository.findById(dstAcctId)
            .toEither { BankingError.AccountNotFound(dstAcctId) }.bind()

        val transferId = TransferId(UUID.randomUUID().toString())
        val receipt = TransferReceipt(
            transferId = transferId.value,
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

        transferId
    }

    override fun setMinimumTransferAmount(minimumTransferAmount: Double) {
        this.minimumTransferAmount = minimumTransferAmount
    }

    override fun setTimeService(timeService: TimeServicePort) {
        this.timeService = timeService
    }
}
