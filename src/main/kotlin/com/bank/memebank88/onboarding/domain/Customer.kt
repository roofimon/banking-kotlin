package com.bank.memebank88.onboarding.domain

import java.time.Instant

/**
 * A customer record created when onboarding is approved. Captures the applicant's identity and the
 * account provisioned for them, so the bank retains who owns each new account beyond the transient
 * onboarding session.
 */
data class Customer(
    val accountId: String,
    val email: String,
    val name: String,
    val phone: String,
    val password: String,
    val creditScore: Int,
    val createdAt: Instant,
)
