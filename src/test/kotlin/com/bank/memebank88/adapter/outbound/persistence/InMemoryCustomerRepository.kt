package com.bank.memebank88.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.domain.Customer
import com.bank.memebank88.port.outbound.CustomerRepositoryPort

/** In-memory [CustomerRepositoryPort] for use-case tests. */
class InMemoryCustomerRepository : CustomerRepositoryPort {
    private val store = mutableMapOf<String, Customer>()

    override fun save(customer: Customer) {
        store[customer.accountId] = customer
    }

    override fun findByAccountId(accountId: String): Option<Customer> = store[accountId].toOption()

    override fun deleteAll() = store.clear()
}
