package com.bank.memebank88.banking.adapter.outbound.service

import com.bank.memebank88.banking.port.outbound.FeePolicyPort

class VariableFeePolicy(
    private val maxFreeFee: Double,
    private val maxPercentFee: Double,
    private val percentage: Double,
    private val flatRate: Double
) : FeePolicyPort {

    override fun calculateFee(transferAmount: Double): Double {
        if (transferAmount <= maxFreeFee) return 0.00
        if (transferAmount <= maxPercentFee) return (transferAmount * percentage / 100)

        return flatRate
    }
}
