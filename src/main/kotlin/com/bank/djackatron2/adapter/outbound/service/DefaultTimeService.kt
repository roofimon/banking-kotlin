package com.bank.djackatron2.adapter.outbound.service

import com.bank.djackatron2.port.outbound.TimeServicePort
import java.time.LocalTime

class DefaultTimeService(
    private val openService: LocalTime,
    private val closeService: LocalTime,
) : TimeServicePort {
    override fun isServiceAvailable(testTime: LocalTime): Boolean {
        return testTime.isAfter(openService) && testTime.isBefore(closeService)
    }
}
