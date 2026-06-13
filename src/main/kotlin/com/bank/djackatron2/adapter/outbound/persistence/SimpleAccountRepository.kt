package com.bank.djackatron2.adapter.outbound.persistence

import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.domain.Account
import javax.security.auth.login.AccountNotFoundException

class SimpleAccountRepository : AccountRepositoryPort {

    companion object {
        const val A123_ID: String = "A123"
        const val C456_ID: String = "C456"
        const val Z999_ID: String = "Z999" // NON EXISTENT ID
        const val A123_INITIAL_BAL: Double = 100.00
        const val C456_INITIAL_BAL: Double = 0.00
    }

    private val accountsById = mapOf(
        A123_ID to Account(A123_ID, A123_INITIAL_BAL),
        C456_ID to Account(C456_ID, C456_INITIAL_BAL)
    )

    override fun findById(srcAcctId: String): Account = nullSafeAccountLookup(srcAcctId)

    override fun updateBalance(account: Account) {
        val actualAccount = nullSafeAccountLookup(account.getId())
        actualAccount.setBalance(account.getBalance())
    }

    private fun nullSafeAccountLookup(acctId: String): Account {
        return accountsById[acctId] ?: throw AccountNotFoundException(acctId)
    }
}
