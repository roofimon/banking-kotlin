package com.bank.memebank88.banking.port.inbound

import arrow.core.Either
import com.bank.memebank88.banking.domain.BankingError
import com.bank.memebank88.banking.domain.TransferId
import com.bank.memebank88.banking.domain.TransferReceipt

/**
 * Primary **inbound port** of the hexagonal architecture.
 *
 * This interface is the sole boundary between anything that _drives_ the application
 * (REST controllers, CLI runners, message consumers, schedulers) and the application core.
 * Inbound adapters must depend on this interface — never on the concrete use-case class.
 *
 * Known inbound adapters: [com.bank.memebank88.banking.adapter.inbound.web.AccountController]
 *
 * Outbound dependencies wired into the implementation:
 * - [com.bank.memebank88.banking.port.outbound.AccountRepositoryPort] — account persistence
 * - [com.bank.memebank88.banking.port.outbound.FeePolicyPort] — fee calculation strategy
 * - [com.bank.memebank88.banking.port.outbound.TimeServicePort] — optional service-hours guard
 */
interface TransferUseCase {

    /**
     * Moves [TransferCommand.amount] from [TransferCommand.srcAcctId] to [TransferCommand.dstAcctId].
     *
     * Execution contract (in order):
     * 1. Validates that the amount is ≥ the configured minimum transfer amount.
     * 2. If a [com.bank.memebank88.banking.port.outbound.TimeServicePort] is wired,
     *    checks that the transfer service is currently available.
     * 3. Loads both accounts from the repository.
     * 4. Calculates the fee via the fee-policy adapter and debits it from the source account
     *    (fee-debit failure is silently swallowed — the transfer still proceeds).
     * 5. Debits the amount from the source account (fails with [BankingError.InsufficientFunds]).
     * 6. Credits the amount to the destination account.
     * 7. Persists the updated balances for both accounts.
     * 8. Publishes a `TransferCompletedEvent` (carrying the [TransferReceipt]) to the internal bus;
     *    a worker delivers the receipt out-of-band. The receipt is **not** returned to the caller.
     *
     * @param command      The transfer inputs (amount, source and destination account ids).
     * @return             [Either.Right] with the accepted transfer's [TransferId] once the transfer
     *                     is applied and the `TransferCompletedEvent` is published (the receipt is
     *                     delivered out-of-band), or [Either.Left] with a [BankingError]:
     *                     - [BankingError.BelowMinimum]     if the amount is below the minimum threshold.
     *                     - [BankingError.OutOfService]     if the time-service guard is active and the
     *                       current time falls outside the configured service window.
     *                     - [BankingError.AccountNotFound]  if either account id is unknown.
     *                     - [BankingError.InsufficientFunds] if the source balance cannot cover the amount
     *                       (after any fee has been attempted).
     */
    fun transfer(command: TransferCommand): Either<BankingError, TransferId>

    /**
     * Sets the lower bound for accepted transfer amounts.
     *
     * Any call to [transfer] with an amount strictly below [minimumTransferAmount] returns
     * [Either.Left] with [BankingError.BelowMinimum] before any account or fee logic is executed.
     *
     * Default value used by the implementation: **1.00**.
     *
     * @param minimumTransferAmount  The new minimum. Must be > 0.
     */
    fun setMinimumTransferAmount(minimumTransferAmount: Double)

    /**
     * Wires an optional time-availability guard into the use case.
     *
     * When set, every call to [transfer] will first invoke
     * [com.bank.memebank88.banking.port.outbound.TimeServicePort.isServiceAvailable]
     * with the current wall-clock time. If the service is unavailable, the transfer is
     * rejected with [Either.Left] carrying [BankingError.OutOfService].
     *
     * If this method is never called, the time guard is disabled and transfers are accepted
     * at any time.
     *
     * @param timeService  The time-availability adapter to use.
     */
    fun setTimeService(timeService: com.bank.memebank88.banking.port.outbound.TimeServicePort)
}
