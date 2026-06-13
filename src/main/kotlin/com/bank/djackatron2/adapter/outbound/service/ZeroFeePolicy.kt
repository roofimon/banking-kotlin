package com.bank.djackatron2.adapter.outbound.service

import com.bank.djackatron2.port.outbound.FeePolicyPort

class ZeroFeePolicy : FeePolicyPort {

    override fun calculateFee(transferAmount: Double): Double {
        return ZERO_AMOUNT
    }

    companion object {
        private const val ZERO_AMOUNT: Double = 0.00
    }
}
