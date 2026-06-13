package com.bank.djackatron2.adapter.outbound.persistence

import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.event.AccountCreditedEvent
import com.bank.djackatron2.domain.event.AccountDebitedEvent
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import javax.security.auth.login.AccountNotFoundException

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

    override fun findById(accountId: String): Account {
        val initial = initialBalances[accountId] ?: throw AccountNotFoundException(accountId)
        val currentBalance = eventStore.eventsFor(accountId).fold(initial) { balance, event ->
            when (event) {
                is AccountCreditedEvent -> balance + event.amount
                is AccountDebitedEvent -> balance - event.amount
            }
        }
        return Account(accountId, currentBalance)
    }
}
