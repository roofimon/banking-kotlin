package com.bank.djackatron2.application.port.outbound

import java.time.LocalTime

interface TimeServicePort {
    fun isServiceAvailable(testTime: LocalTime): Boolean
}
