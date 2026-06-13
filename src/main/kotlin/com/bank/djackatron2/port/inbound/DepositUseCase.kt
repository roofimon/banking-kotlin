package com.bank.djackatron2.port.inbound

import com.bank.djackatron2.domain.DepositReceipt

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
     * @return           A [DepositReceipt] with the deposit amount and final balance.
     *
     * @throws IllegalArgumentException                          if [amount] is below the minimum.
     * @throws javax.security.auth.login.AccountNotFoundException if [accountId] does not exist.
     */
    fun deposit(amount: Double, accountId: String): DepositReceipt

    /**
     * Sets the lower bound for accepted deposit amounts. Default: **0.01**.
     */
    fun setMinimumDepositAmount(minimumDepositAmount: Double)
}
