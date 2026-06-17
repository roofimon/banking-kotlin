package com.bank.memebank88.banking.port.outbound

import arrow.core.Option
import com.bank.memebank88.banking.domain.Account

interface AccountRepositoryPort {
    /** Returns [arrow.core.Some] with the account, or [arrow.core.None] if no account exists for [srcAcctId]. */
    fun findById(srcAcctId: String): Option<Account>
}
