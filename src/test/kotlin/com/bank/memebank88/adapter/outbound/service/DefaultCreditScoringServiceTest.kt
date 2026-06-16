package com.bank.memebank88.adapter.outbound.service

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class DefaultCreditScoringServiceTest {

    private val service = DefaultCreditScoringService()

    @Test
    fun scoreIsDeterministicAndWithinRange() {
        val first = service.assess(50000.0, "SALARIED", 2000.0, 100000.0)
        val second = service.assess(50000.0, "SALARIED", 2000.0, 100000.0)

        assertThat(first, equalTo(second))
        assertThat(first.score in 300..899, equalTo(true))
    }

    @Test
    fun strongFinancesAreApproved() {
        val decision = service.assess(200000.0, "PROFESSIONAL", 1000.0, 1_000_000.0)

        assertThat(decision.score >= DefaultCreditScoringService.APPROVAL_THRESHOLD, equalTo(true))
        assertThat(decision.approved, equalTo(true))
    }

    @Test
    fun weakFinancesAreRejected() {
        val decision = service.assess(12000.0, "UNEMPLOYED", 15000.0, 0.0)

        assertThat(decision.score < DefaultCreditScoringService.APPROVAL_THRESHOLD, equalTo(true))
        assertThat(decision.approved, equalTo(false))
    }

    @Test
    fun zeroSalaryDoesNotBlowUp() {
        val decision = service.assess(0.0, "OTHER", 1000.0, 0.0)

        assertThat(decision.score in 300..899, equalTo(true))
    }
}
