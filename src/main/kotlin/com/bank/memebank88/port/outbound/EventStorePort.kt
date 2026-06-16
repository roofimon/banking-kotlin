package com.bank.memebank88.port.outbound

import com.bank.memebank88.domain.event.AccountEvent

interface EventStorePort {
    fun append(event: AccountEvent)
    fun eventsFor(accountId: String): List<AccountEvent>
    fun clearAll()
}
