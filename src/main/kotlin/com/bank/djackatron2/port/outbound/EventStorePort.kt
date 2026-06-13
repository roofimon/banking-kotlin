package com.bank.djackatron2.port.outbound

import com.bank.djackatron2.domain.event.AccountEvent

interface EventStorePort {
    fun append(event: AccountEvent)
    fun eventsFor(accountId: String): List<AccountEvent>
    fun clearAll()
}
