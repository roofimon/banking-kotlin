package com.bank.memebank88.onboarding.port.outbound

/** Outbound port for creating a new bank account (used when onboarding is approved). */
interface AccountProvisioningPort {
    /** Creates a new account with [initialBalance] and returns its generated id. */
    fun createAccount(initialBalance: Double): String
}
