package com.bank.memebank88.domain

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

    /** No onboarding session exists for [onboardingId]. */
    data class OnboardingNotFound(val onboardingId: String) : DomainError

    /** An onboarding step was attempted in the wrong order. */
    data class OnboardingStepOutOfOrder(val expected: String, val actual: String) : DomainError

    /** A verification code/token did not match. [kind] is "email" or "token". */
    data class VerificationFailed(val kind: String) : DomainError

    /** Customer info (name/phone) failed validation. */
    data class InvalidCustomerInfo(val reason: String) : DomainError

    /** Login email/password did not match any customer. */
    data object InvalidCredentials : DomainError
}
