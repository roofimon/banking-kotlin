package com.bank.djackatron2.domain.event

import java.time.Instant

sealed class AccountEvent(
    val accountId: String,
    val amount: Double,
    val occurredAt: Instant
)

class AccountCreditedEvent(accountId: String, amount: Double, occurredAt: Instant) :
    AccountEvent(accountId, amount, occurredAt)

class AccountDebitedEvent(accountId: String, amount: Double, occurredAt: Instant) :
    AccountEvent(accountId, amount, occurredAt)
