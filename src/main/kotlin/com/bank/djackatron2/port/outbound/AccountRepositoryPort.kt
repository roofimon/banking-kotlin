package com.bank.djackatron2.port.outbound

import com.bank.djackatron2.domain.Account

interface AccountRepositoryPort {
    fun findById(srcAcctId: String): Account
}
