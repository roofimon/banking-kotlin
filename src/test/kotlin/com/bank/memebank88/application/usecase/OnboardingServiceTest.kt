package com.bank.memebank88.application.usecase

import com.bank.memebank88.adapter.outbound.persistence.InMemoryCustomerRepository
import com.bank.memebank88.adapter.outbound.persistence.InMemoryOnboardingRepository
import com.bank.memebank88.domain.DomainError
import com.bank.memebank88.domain.OnboardingStatus
import com.bank.memebank88.port.outbound.AccountProvisioningPort
import com.bank.memebank88.port.outbound.CreditDecision
import com.bank.memebank88.port.outbound.CreditScoringPort
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class OnboardingServiceTest {

    private lateinit var repository: InMemoryOnboardingRepository
    private lateinit var customers: InMemoryCustomerRepository
    private lateinit var creditScoring: CreditScoringPort
    private lateinit var accountProvisioning: AccountProvisioningPort
    private lateinit var service: OnboardingService

    @BeforeEach
    fun setUp() {
        repository = InMemoryOnboardingRepository()
        customers = InMemoryCustomerRepository()
        creditScoring = mock(CreditScoringPort::class.java)
        accountProvisioning = mock(AccountProvisioningPort::class.java)
        service = OnboardingService(repository, creditScoring, accountProvisioning, customers)
    }

    /** Drives the flow up to (but not including) scoring; returns the onboarding id. */
    private fun reachTokenVerified(): String {
        val started = service.start("jane@example.com").getOrNull()!!
        service.verifyEmail(started.id, started.emailCode)
        val info = service.submitInfo(started.id, "Jane Doe", "0812345678").getOrNull()!!
        service.verifyToken(started.id, info.sessionToken!!)
        return started.id
    }

    @Test
    fun happyPathCompletesAndCreatesAccount() {
        `when`(creditScoring.assess(any(), any(), any(), any())).thenReturn(CreditDecision(720, approved = true))
        `when`(accountProvisioning.createAccount(0.0)).thenReturn("AC0000001")

        val id = reachTokenVerified()
        val result = service.score(id, 50000.0, "SALARIED", 2000.0, 100000.0).getOrNull()!!

        assertThat(result.status, equalTo(OnboardingStatus.COMPLETED))
        assertThat(result.creditScore, equalTo(720))
        assertThat(result.accountId, equalTo("AC0000001"))
        verify(accountProvisioning).createAccount(0.0)

        val customer = customers.findByAccountId("AC0000001").getOrNull()!!
        assertThat(customer.email, equalTo("jane@example.com"))
        assertThat(customer.name, equalTo("Jane Doe"))
        assertThat(customer.phone, equalTo("0812345678"))
        assertThat(customer.creditScore, equalTo(720))
        assertThat(customer.password.length, equalTo(5))
        assertThat(result.password, equalTo(customer.password))
    }

    @Test
    fun rejectedScoreDoesNotCreateAccount() {
        `when`(creditScoring.assess(any(), any(), any(), any())).thenReturn(CreditDecision(450, approved = false))

        val id = reachTokenVerified()
        val result = service.score(id, 12000.0, "UNEMPLOYED", 15000.0, 0.0).getOrNull()!!

        assertThat(result.status, equalTo(OnboardingStatus.REJECTED))
        assertThat(result.creditScore, equalTo(450))
        assertThat(result.accountId, nullValue())
        verify(accountProvisioning, never()).createAccount(anyDouble())
        assertThat(customers.findByAccountId("AC0000001").getOrNull(), nullValue())
        assertThat(result.password, nullValue())
    }

    @Test
    fun wrongEmailCodeFails() {
        val started = service.start("jane@example.com").getOrNull()!!
        val error = service.verifyEmail(started.id, "000000").leftOrNull()
        assertThat(error, instanceOf(DomainError.VerificationFailed::class.java))
    }

    @Test
    fun stepOutOfOrderFails() {
        val started = service.start("jane@example.com").getOrNull()!!
        // submit info before verifying email
        val error = service.submitInfo(started.id, "Jane", "0812345678").leftOrNull()
        assertThat(error, instanceOf(DomainError.OnboardingStepOutOfOrder::class.java))
    }

    @Test
    fun unknownOnboardingFails() {
        val error = service.verifyEmail("does-not-exist", "123456").leftOrNull()
        assertThat(error, instanceOf(DomainError.OnboardingNotFound::class.java))
    }

    @Test
    fun blankInfoFails() {
        val started = service.start("jane@example.com").getOrNull()!!
        service.verifyEmail(started.id, started.emailCode)
        val error = service.submitInfo(started.id, "  ", "0812345678").leftOrNull()
        assertThat(error, instanceOf(DomainError.InvalidCustomerInfo::class.java))
    }
}
