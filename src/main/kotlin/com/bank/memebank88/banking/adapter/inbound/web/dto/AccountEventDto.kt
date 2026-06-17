package com.bank.memebank88.banking.adapter.inbound.web.dto

data class AccountEventDto(
    val eventType: String,
    val amount: Double,
    val occurredAt: String,
)
