package com.bank.djackatron2.adapter.outbound.persistence

import com.bank.djackatron2.domain.event.AccountEvent
import com.bank.djackatron2.port.outbound.EventStorePort

class InMemoryEventStore : EventStorePort {

    private val events = mutableListOf<AccountEvent>()

    override fun append(event: AccountEvent) {
        events.add(event)
    }

    override fun eventsFor(accountId: String): List<AccountEvent> =
        events.filter { it.accountId == accountId }

    override fun clearAll() {
        events.clear()
    }
}
