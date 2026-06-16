package com.bank.memebank88.port.outbound

/** Outbound port for credit scoring. The default adapter is a deterministic stub. */
interface CreditScoringPort {
    fun assess(salary: Double, occupation: String, monthlyCost: Double, totalWealth: Double): CreditDecision
}

/** Outcome of a credit assessment. Rejection is a valid result, not an error. */
data class CreditDecision(val score: Int, val approved: Boolean)
