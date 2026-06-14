package com.bank.djackatron2.port.outbound

import arrow.core.Option
import com.bank.djackatron2.domain.Account

interface AccountRepositoryPort {
    /** Returns [arrow.core.Some] with the account, or [arrow.core.None] if no account exists for [srcAcctId]. */
    fun findById(srcAcctId: String): Option<Account>
}
