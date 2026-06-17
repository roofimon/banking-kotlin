package com.bank.memebank88.banking.adapter.outbound.service

import com.bank.memebank88.banking.port.outbound.TimeServicePort
import java.time.LocalTime

class DefaultTimeService(
    private val openService: LocalTime,
    private val closeService: LocalTime,
) : TimeServicePort {
    override fun isServiceAvailable(testTime: LocalTime): Boolean {
        return testTime.isAfter(openService) && testTime.isBefore(closeService)
    }
}
