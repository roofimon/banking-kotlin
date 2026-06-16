package com.bank.memebank88.adapter.outbound.service

import com.bank.memebank88.port.outbound.CreditDecision
import com.bank.memebank88.port.outbound.CreditScoringPort
import org.springframework.stereotype.Service
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Deterministic credit scoring derived from the applicant's finances. Starts from a 300 floor and
 * adds bounded contributions for salary, total wealth, disposable-income ratio, and occupation,
 * then clamps to [300, 899]. Rewards higher salary/wealth and penalizes a high monthly cost
 * relative to salary. Approves at or above [APPROVAL_THRESHOLD]. Same inputs always yield the same
 * decision.
 */
@Service
class DefaultCreditScoringService : CreditScoringPort {

    override fun assess(salary: Double, occupation: String, monthlyCost: Double, totalWealth: Double): CreditDecision {
        val disposableRatio = if (salary > 0) (salary - monthlyCost) / salary else -1.0
        val salaryPoints = min(salary / 200.0, 250.0)
        val wealthPoints = min(totalWealth / 2000.0, 200.0)
        val ratioPoints = (disposableRatio * 200).coerceIn(-150.0, 150.0)
        val occupationPoints = occupationBonus(occupation)

        val raw = 300 + salaryPoints + wealthPoints + ratioPoints + occupationPoints
        val score = raw.roundToInt().coerceIn(300, 899)
        return CreditDecision(score = score, approved = score >= APPROVAL_THRESHOLD)
    }

    /** Bounded bonus by occupation category; any unknown/blank value scores zero. */
    private fun occupationBonus(code: String): Double = when (code.trim().uppercase()) {
        "SALARIED", "PROFESSIONAL" -> 50.0
        "SELF_EMPLOYED" -> 25.0
        "FREELANCER" -> 10.0
        else -> 0.0
    }

    companion object {
        const val APPROVAL_THRESHOLD = 600
    }
}
