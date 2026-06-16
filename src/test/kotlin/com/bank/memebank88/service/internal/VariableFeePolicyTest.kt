package com.bank.memebank88.adapter.outbound.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VariableFeePolicyTest {

    @Test
    fun testVariableFeePolicy() {
        val feePolicy = VariableFeePolicy(1000.00, 1000000.00, 1.00, 20000.00)

        //1,000,001 up flat rate 2,0000
        assertEquals(feePolicy.calculateFee(1000001.00), 20000.00)

        //1,001 - 1,000,000 percent 1%
        assertEquals(feePolicy.calculateFee(1000000.00), 10000.00)
        assertEquals(feePolicy.calculateFee(1001.00), 10.01)

        //1000 down free 0
        assertEquals(feePolicy.calculateFee(1000.00), 0.00)
    }
}