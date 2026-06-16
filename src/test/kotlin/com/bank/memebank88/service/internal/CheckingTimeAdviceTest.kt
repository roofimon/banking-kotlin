package com.bank.memebank88.adapter.outbound.service

import com.bank.memebank88.application.exception.OutOfServiceException
import com.bank.memebank88.port.outbound.TimeServicePort
import org.aopalliance.intercept.MethodInvocation
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.LocalTime

class CheckingTimeAdviceTest {

    private val invocation = mock(MethodInvocation::class.java)
    private val timeService = mock(TimeServicePort::class.java)

    @Test
    fun testInvoke() {
        val advice = CheckingTimeAdvice(timeService)
        `when`(timeService.isServiceAvailable(any<LocalTime>())).thenReturn(true)

        val result = advice.invoke(invocation)

        assertNull(result)
        verify(timeService).isServiceAvailable(any<LocalTime>())
    }

    @Test
    @Throws(Throwable::class)
    fun testInvokeWithTimeOutOfService() {
        //given
        val advice = CheckingTimeAdvice(timeService)
        `when`(timeService.isServiceAvailable(any<LocalTime>())).thenReturn(false)

        //when
        try {
            advice.invoke(invocation)
            fail()
        } catch (e: OutOfServiceException) {
            //then
            verify(timeService).isServiceAvailable(any<LocalTime>())
        }
    }
}