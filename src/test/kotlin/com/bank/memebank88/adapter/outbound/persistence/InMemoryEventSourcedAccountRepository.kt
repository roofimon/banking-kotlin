package com.bank.memebank88.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.domain.Account
import com.bank.memebank88.domain.event.AccountCreditedEvent
import com.bank.memebank88.domain.event.AccountDebitedEvent
import com.bank.memebank88.port.outbound.AccountRepositoryPort

class InMemoryEventSourcedAccountRepository(
    initialBalances: Map<String, Double>,
    private val eventStore: InMemoryEventStore
) : AccountRepositoryPort {

    companion object {
        const val A123_ID = "A123"
        const val C456_ID = "C456"
        const val Z999_ID = "Z999"
        const val A123_INITIAL_BAL = 100.00
        const val C456_INITIAL_BAL = 0.00
    }

    private val initialBalances: Map<String, Double> = initialBalances.toMap()

    override fun findById(accountId: String): Option<Account> =
        initialBalances[accountId].toOption().map { initial ->
            val currentBalance = eventStore.eventsFor(accountId).fold(initial) { balance, event ->
                when (event) {
                    is AccountCreditedEvent -> balance + event.amount
                    is AccountDebitedEvent -> balance - event.amount
                }
            }
            Account(accountId, currentBalance)
        }
}
