package com.bank.djackatron2.adapter.outbound.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlatFeePolicyTest {

    @Test
    fun testFlatFeePolicy() {
        val feePolicy = FlatFeePolicy(5.00)

        assertEquals(feePolicy.calculateFee(1000.00), 5.00)
        assertEquals(feePolicy.calculateFee(10.00), 5.00)
        assertEquals(feePolicy.calculateFee(1.00), 5.00)
    }
}