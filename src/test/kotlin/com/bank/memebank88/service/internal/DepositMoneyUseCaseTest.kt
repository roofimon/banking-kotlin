package com.bank.memebank88.banking.application.usecase

import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository
import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.A123_ID
import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.A123_INITIAL_BAL
import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.C456_ID
import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.C456_INITIAL_BAL
import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.Z999_ID
import com.bank.memebank88.banking.adapter.outbound.persistence.InMemoryEventStore
import com.bank.memebank88.banking.domain.BankingError
import com.bank.memebank88.banking.port.inbound.DepositUseCase
import com.bank.memebank88.banking.port.outbound.AccountRepositoryPort
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class DepositMoneyUseCaseTest {

    private lateinit var eventStore: InMemoryEventStore
    private lateinit var accountRepository: AccountRepositoryPort
    private lateinit var depositService: DepositUseCase

    @BeforeEach
    fun setUp() {
        eventStore = InMemoryEventStore()
        accountRepository = InMemoryEventSourcedAccountRepository(
            mapOf(A123_ID to A123_INITIAL_BAL, C456_ID to C456_INITIAL_BAL),
            eventStore
        )
        depositService = DepositMoneyUseCase(accountRepository, eventStore)

        assertThat(accountRepository.findById(A123_ID).getOrNull()!!.getBalance(), equalTo(A123_INITIAL_BAL))
        assertThat(accountRepository.findById(C456_ID).getOrNull()!!.getBalance(), equalTo(C456_INITIAL_BAL))
    }

    @Test
    fun testDeposit() {
        val depositAmount = 50.00

        val receipt = depositService.deposit(depositAmount, C456_ID).getOrNull()!!

        assertThat(receipt.getDepositAmount(), equalTo(depositAmount))
        assertThat(receipt.getFinalAccount().getBalance(), equalTo(C456_INITIAL_BAL + depositAmount))
        assertThat(accountRepository.findById(C456_ID).getOrNull()!!.getBalance(), equalTo(C456_INITIAL_BAL + depositAmount))
    }

    @Test
    fun testDepositToNonZeroBalance() {
        val depositAmount = 20.00

        val receipt = depositService.deposit(depositAmount, A123_ID).getOrNull()!!

        assertThat(receipt.getDepositAmount(), equalTo(depositAmount))
        assertThat(receipt.getFinalAccount().getBalance(), equalTo(A123_INITIAL_BAL + depositAmount))
        assertThat(accountRepository.findById(A123_ID).getOrNull()!!.getBalance(), equalTo(A123_INITIAL_BAL + depositAmount))
    }

    @Test
    fun testNonExistentAccount() {
        val error = depositService.deposit(10.00, Z999_ID).leftOrNull()
        assertThat(error, instanceOf(BankingError.AccountNotFound::class.java))
    }

    @Test
    fun testZeroDepositAmount() {
        val error = depositService.deposit(0.00, C456_ID).leftOrNull()
        assertThat(error, instanceOf(BankingError.BelowMinimum::class.java))
    }

    @Test
    fun testNegativeDepositAmount() {
        val error = depositService.deposit(-10.00, C456_ID).leftOrNull()
        assertThat(error, instanceOf(BankingError.BelowMinimum::class.java))
    }

    @Test
    fun testDepositAmountLessThanMinimum() {
        val error = depositService.deposit(0.001, C456_ID).leftOrNull()
        assertThat(error, instanceOf(BankingError.BelowMinimum::class.java))
    }

    @Test
    fun testCustomizedMinimumDepositAmount() {
        depositService.deposit(1.00, C456_ID)
        depositService.setMinimumDepositAmount(5.00)
        depositService.deposit(5.00, C456_ID)

        val error = depositService.deposit(3.00, C456_ID).leftOrNull()
        assertThat(
            "expected BelowMinimum on 3.00 deposit that violates 5.00 minimum",
            error, instanceOf(BankingError.BelowMinimum::class.java)
        )
    }
}
