package com.bank.djackatron2.adapter.inbound.web.dto

data class AccountEventDto(
    val eventType: String,
    val amount: Double,
    val occurredAt: String,
)
