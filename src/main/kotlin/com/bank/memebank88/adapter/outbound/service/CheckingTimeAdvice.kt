package com.bank.memebank88.adapter.outbound.service

import com.bank.memebank88.application.exception.OutOfServiceException
import com.bank.memebank88.port.outbound.TimeServicePort
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import mu.KotlinLogging
import java.time.LocalTime

private val logger = KotlinLogging.logger {}

class CheckingTimeAdvice(
    private val timeService: TimeServicePort
) : MethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        logger.info { "Checking Time Service" }
        if (timeService.isServiceAvailable(LocalTime.now())) {
            return invocation.proceed()
        } else {
            throw OutOfServiceException()
        }
    }
}
