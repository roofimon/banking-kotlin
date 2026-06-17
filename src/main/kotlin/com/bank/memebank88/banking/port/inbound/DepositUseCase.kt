package com.bank.memebank88.banking.port.inbound

import arrow.core.Either
import com.bank.memebank88.banking.domain.DepositReceipt
import com.bank.memebank88.banking.domain.BankingError

/**
 * Primary **inbound port** for the deposit operation.
 *
 * Inbound adapters must depend on this interface — never on the concrete use-case class.
 *
 * Known inbound adapters: [com.bank.memebank88.banking.adapter.inbound.web.AccountController]
 *
 * Outbound dependency wired into the implementation:
 * - [com.bank.memebank88.banking.port.outbound.AccountRepositoryPort] — account persistence
 */
interface DepositUseCase {

    /**
     * Credits [amount] to the account identified by [accountId].
     *
     * @param amount     The amount to deposit. Must be ≥ the configured minimum (default 0.01).
     * @param accountId  ID of the account to credit.
     * @return           [Either.Right] with a [DepositReceipt] on success, or [Either.Left] with a
     *                   [BankingError]: [BankingError.BelowMinimum] if [amount] is below the minimum,
     *                   or [BankingError.AccountNotFound] if [accountId] does not exist.
     */
    fun deposit(amount: Double, accountId: String): Either<BankingError, DepositReceipt>

    /**
     * Sets the lower bound for accepted deposit amounts. Default: **0.01**.
     */
    fun setMinimumDepositAmount(minimumDepositAmount: Double)
}
