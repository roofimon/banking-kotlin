package com.bank.djackatron2.application.port.outbound

import com.bank.djackatron2.domain.Account

interface AccountRepositoryPort {
    fun findById(srcAcctId: String): Account
    fun updateBalance(account: Account)
}
