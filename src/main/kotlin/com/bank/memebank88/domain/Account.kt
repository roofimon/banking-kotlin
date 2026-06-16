package com.bank.memebank88.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.bank.memebank88.domain.event.AccountCreditedEvent
import com.bank.memebank88.domain.event.AccountDebitedEvent
import com.bank.memebank88.domain.event.AccountEvent
import java.time.Instant

data class Account(
    private val id: String,
    private var balance: Double
) {
    private val _domainEvents = mutableListOf<AccountEvent>()

    companion object {
        fun copy(account: Account) = Account(
            id = account.id,
            balance = account.balance
        )
    }

    fun getId(): String = id
    fun getBalance(): Double = balance

    fun domainEvents(): List<AccountEvent> = _domainEvents.toList()
    fun clearDomainEvents() { _domainEvents.clear() }

    fun setBalance(balance: Double) {
        this.balance = balance
    }

    fun debit(amount: Double): Either<DomainError, Unit> = either {
        ensure(amount > 0.00) { DomainError.InvalidAmount(amount) }
        ensure(amount <= balance) { DomainError.InsufficientFunds(id, amount, balance) }
        balance -= amount
        _domainEvents.add(AccountDebitedEvent(id, amount, Instant.now()))
    }

    fun credit(amount: Double): Either<DomainError, Unit> = either {
        ensure(amount > 0.00) { DomainError.InvalidAmount(amount) }
        balance += amount
        _domainEvents.add(AccountCreditedEvent(id, amount, Instant.now()))
    }
}
