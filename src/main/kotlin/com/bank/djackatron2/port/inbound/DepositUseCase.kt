package com.bank.djackatron2.port.inbound

import arrow.core.Either
import com.bank.djackatron2.domain.DepositReceipt
import com.bank.djackatron2.domain.DomainError

/**
 * Primary **inbound port** for the deposit operation.
 *
 * Inbound adapters must depend on this interface — never on the concrete use-case class.
 *
 * Known inbound adapters: [com.bank.djackatron2.adapter.inbound.web.AccountController]
 *
 * Outbound dependency wired into the implementation:
 * - [com.bank.djackatron2.port.outbound.AccountRepositoryPort] — account persistence
 */
interface DepositUseCase {

    /**
     * Credits [amount] to the account identified by [accountId].
     *
     * @param amount     The amount to deposit. Must be ≥ the configured minimum (default 0.01).
     * @param accountId  ID of the account to credit.
     * @return           [Either.Right] with a [DepositReceipt] on success, or [Either.Left] with a
     *                   [DomainError]: [DomainError.BelowMinimum] if [amount] is below the minimum,
     *                   or [DomainError.AccountNotFound] if [accountId] does not exist.
     */
    fun deposit(amount: Double, accountId: String): Either<DomainError, DepositReceipt>

    /**
     * Sets the lower bound for accepted deposit amounts. Default: **0.01**.
     */
    fun setMinimumDepositAmount(minimumDepositAmount: Double)
}
