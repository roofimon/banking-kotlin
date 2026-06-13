package com.bank.djackatron2.domain

import com.bank.djackatron2.domain.event.AccountCreditedEvent
import com.bank.djackatron2.domain.event.AccountDebitedEvent
import com.bank.djackatron2.domain.event.AccountEvent
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

    fun debit(amount: Double) {
        assertValid(amount)
        if (amount > balance) throw InsufficientFundsException(this, amount)
        balance -= amount
        _domainEvents.add(AccountDebitedEvent(id, amount, Instant.now()))
    }

    fun credit(amount: Double) {
        assertValid(amount)
        balance += amount
        _domainEvents.add(AccountCreditedEvent(id, amount, Instant.now()))
    }

    private fun assertValid(amount: Double) = require((amount > 0.00)) { "amount must be greater than zero" }
}
