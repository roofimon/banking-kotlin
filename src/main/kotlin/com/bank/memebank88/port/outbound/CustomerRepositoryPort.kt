package com.bank.memebank88.port.outbound

import arrow.core.Option
import com.bank.memebank88.domain.Customer

/** Outbound port for persisting customers created when onboarding is approved. */
interface CustomerRepositoryPort {
    fun save(customer: Customer)
    fun findByAccountId(accountId: String): Option<Customer>
    fun deleteAll()
}
