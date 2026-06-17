package com.bank.memebank88.banking.domain

/**
 * The banking module's failure vocabulary, modelled as data rather than thrown exceptions.
 *
 * Operations that can fail return `Either<BankingError, T>` (see [Account.debit], [Account.credit]
 * and the transfer/deposit use-case ports). A missing account is modelled separately as
 * `Option<Account>` at the repository port.
 */
sealed interface BankingError {

    /** Amount was zero or negative. */
    data class InvalidAmount(val amount: Double) : BankingError

    /** Amount was positive but below the configured minimum for [operation] (e.g. "deposit"). */
    data class BelowMinimum(val amount: Double, val minimum: Double, val operation: String) : BankingError

    /** The source account balance cannot cover [attempted]. */
    data class InsufficientFunds(val accountId: String, val attempted: Double, val balance: Double) : BankingError

    /** No account exists for [accountId]. */
    data class AccountNotFound(val accountId: String) : BankingError

    /** The time-service guard rejected the operation outside the service window. */
    data object OutOfService : BankingError
}
