package com.bank.memebank88.banking.application.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.bank.memebank88.banking.domain.Account
import com.bank.memebank88.banking.domain.DepositReceipt
import com.bank.memebank88.banking.domain.BankingError
import com.bank.memebank88.banking.port.inbound.DepositUseCase
import com.bank.memebank88.banking.port.outbound.AccountRepositoryPort
import com.bank.memebank88.banking.port.outbound.EventStorePort
import org.springframework.stereotype.Service

@Service
class DepositMoneyUseCase(
    private val accountRepository: AccountRepositoryPort,
    private val eventStore: EventStorePort,
) : DepositUseCase {

    private var minimumDepositAmount = 0.01

    override fun deposit(amount: Double, accountId: String): Either<BankingError, DepositReceipt> = either {
        ensure(amount >= minimumDepositAmount) {
            BankingError.BelowMinimum(amount, minimumDepositAmount, "deposit")
        }

        val account = accountRepository.findById(accountId)
            .toEither { BankingError.AccountNotFound(accountId) }
            .bind()
        val initial = Account.copy(account)

        account.credit(amount).bind()

        account.domainEvents().forEach { eventStore.append(it) }
        account.clearDomainEvents()

        DepositReceipt(amount, initial, Account.copy(account))
    }

    override fun setMinimumDepositAmount(minimumDepositAmount: Double) {
        this.minimumDepositAmount = minimumDepositAmount
    }
}
