package com.bank.djackatron2.application.event

import com.bank.djackatron2.domain.TransferReceipt

/**
 * Application/integration event published to the internal bus once a transfer has succeeded and
 * its account events are persisted. Carries the [TransferReceipt] so a worker can dispatch it
 * out-of-band. Not persisted in the event store (that remains the AccountEvent log).
 */
data class TransferCompletedEvent(val receipt: TransferReceipt)
