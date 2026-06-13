package com.bank.djackatron2.port.outbound

/**
 * **Outbound fee-strategy port** — encapsulates the pricing model for money transfers.
 *
 * Implements the Strategy pattern: swap adapters to change the fee structure without
 * modifying any business or use-case logic. The active adapter is injected into
 * [com.bank.djackatron2.application.usecase.TransferMoneyUseCase] at construction time.
 *
 * Known adapters:
 * - [com.bank.djackatron2.adapter.outbound.service.ZeroFeePolicy]
 *   — always returns `0.0`; used for free-transfer scenarios and most unit tests.
 * - [com.bank.djackatron2.adapter.outbound.service.FlatFeePolicy]
 *   — charges a fixed amount per transfer (default **$5.00**), regardless of size.
 * - [com.bank.djackatron2.adapter.outbound.service.VariableFeePolicy]
 *   — tiered pricing: free up to a threshold, percentage-based in the mid-range,
 *     and a flat rate for very large transfers.
 */
interface FeePolicyPort {

    /**
     * Calculates the fee to charge the sender for a transfer of [transferAmount].
     *
     * Contract:
     * - Must return a value ≥ `0.0`. Negative fees are not valid.
     * - Returning `0.0` means the transfer is free; the use case skips the fee-debit step.
     * - When the fee is > `0.0`, the use case attempts to debit it from the source account
     *   **before** the principal transfer. If the source account cannot cover the fee,
     *   the debit fails silently and the transfer proceeds without collecting the fee.
     *
     * @param transferAmount  The gross amount being transferred (before any fee deduction).
     * @return                The fee amount to charge, or `0.0` for no fee.
     */
    fun calculateFee(transferAmount: Double): Double
}
