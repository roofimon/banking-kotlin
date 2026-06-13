package com.bank.djackatron2.application.port.outbound

interface FeePolicyPort {
    fun calculateFee(transferAmount: Double): Double
}
