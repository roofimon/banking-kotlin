package com.bank.memebank88.banking.adapter.outbound.service

import com.bank.memebank88.banking.port.outbound.FeePolicyPort
import org.springframework.stereotype.Service

@Service
class FlatFeePolicy(
    private val flatFee: Double = 5.00
) : FeePolicyPort {

    override fun calculateFee(transferAmount: Double): Double {
        return flatFee
    }
}
