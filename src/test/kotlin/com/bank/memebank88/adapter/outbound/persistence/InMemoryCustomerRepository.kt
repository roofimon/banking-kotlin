package com.bank.memebank88.onboarding.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.onboarding.domain.Customer
import com.bank.memebank88.onboarding.port.outbound.CustomerRepositoryPort

/** In-memory [CustomerRepositoryPort] for use-case tests. */
class InMemoryCustomerRepository : CustomerRepositoryPort {
    private val store = mutableMapOf<String, Customer>()

    override fun save(customer: Customer) {
        store[customer.accountId] = customer
    }

    override fun findByAccountId(accountId: String): Option<Customer> = store[accountId].toOption()

    override fun findByEmail(email: String): Option<Customer> =
        store.values.firstOrNull { it.email == email }.toOption()

    override fun deleteAll() = store.clear()
}
