package com.bank.djackatron2.domain

/**
 * The domain's failure vocabulary, modelled as data rather than thrown exceptions.
 *
 * Operations that can fail return `Either<DomainError, T>` (see [Account.debit],
 * [Account.credit] and the inbound use-case ports). A missing account is modelled
 * separately as `Option<Account>` at the repository port.
 *
 * Each case carries enough information for an adapter to render a meaningful response.
 */
sealed interface DomainError {

    /** Amount was zero or negative. */
    data class InvalidAmount(val amount: Double) : DomainError

    /** Amount was positive but below the configured minimum for [operation] (e.g. "deposit"). */
    data class BelowMinimum(val amount: Double, val minimum: Double, val operation: String) : DomainError

    /** The source account balance cannot cover [attempted]. */
    data class InsufficientFunds(val accountId: String, val attempted: Double, val balance: Double) : DomainError

    /** No account exists for [accountId]. */
    data class AccountNotFound(val accountId: String) : DomainError

    /** The time-service guard rejected the operation outside the service window. */
    data object OutOfService : DomainError
}
