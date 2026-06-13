package com.bank.djackatron2.port.outbound

import java.time.LocalTime

/**
 * **Outbound time-availability port** — abstracts the concept of service operating hours.
 *
 * Used in two places:
 * 1. **Use case guard** — [com.bank.djackatron2.application.usecase.TransferMoneyUseCase]
 *    calls [isServiceAvailable] with `LocalTime.now()` at the start of every transfer when a
 *    `TimeServicePort` has been wired via
 *    [com.bank.djackatron2.port.inbound.TransferUseCase.setTimeService].
 *    If unavailable, the use case throws
 *    [com.bank.djackatron2.application.exception.OutOfServiceException].
 * 2. **AOP interceptor** — [com.bank.djackatron2.adapter.outbound.service.CheckingTimeAdvice]
 *    wraps any Spring-proxied method and enforces the same availability check at the
 *    method-invocation level, independent of the use case.
 *
 * Known adapters:
 * - [com.bank.djackatron2.adapter.outbound.service.DefaultTimeService]
 *   — configurable open/close window with **exclusive** bounds:
 *   service is available when `openTime < now < closeTime`.
 */
interface TimeServicePort {

    /**
     * Returns whether the transfer service is available at [testTime].
     *
     * The [testTime] parameter exists deliberately so that tests can pass a fixed time
     * instead of relying on wall-clock time, making service-hour logic fully deterministic.
     * Production callers pass `LocalTime.now()`.
     *
     * When this method returns `false`:
     * - The use case throws [com.bank.djackatron2.application.exception.OutOfServiceException].
     * - The AOP advice ([com.bank.djackatron2.adapter.outbound.service.CheckingTimeAdvice])
     *   also throws [com.bank.djackatron2.application.exception.OutOfServiceException] and
     *   aborts method execution without calling the target method.
     *
     * @param testTime  The time to evaluate against the service window.
     * @return          `true` if the service accepts transfers at [testTime], `false` otherwise.
     */
    fun isServiceAvailable(testTime: LocalTime): Boolean
}
