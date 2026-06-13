package com.bank.djackatron2.application.usecase

import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.A123_ID
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.A123_INITIAL_BAL
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.C456_ID
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.C456_INITIAL_BAL
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.Z999_ID
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventStore
import com.bank.djackatron2.port.inbound.DepositUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import javax.security.auth.login.AccountNotFoundException
import kotlin.test.fail

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

        assertThat(accountRepository.findById(A123_ID).getBalance(), equalTo(A123_INITIAL_BAL))
        assertThat(accountRepository.findById(C456_ID).getBalance(), equalTo(C456_INITIAL_BAL))
    }

    @Test
    fun testDeposit() {
        val depositAmount = 50.00

        val receipt = depositService.deposit(depositAmount, C456_ID)

        assertThat(receipt.getDepositAmount(), equalTo(depositAmount))
        assertThat(receipt.getFinalAccount().getBalance(), equalTo(C456_INITIAL_BAL + depositAmount))
        assertThat(accountRepository.findById(C456_ID).getBalance(), equalTo(C456_INITIAL_BAL + depositAmount))
    }

    @Test
    fun testDepositToNonZeroBalance() {
        val depositAmount = 20.00

        val receipt = depositService.deposit(depositAmount, A123_ID)

        assertThat(receipt.getDepositAmount(), equalTo(depositAmount))
        assertThat(receipt.getFinalAccount().getBalance(), equalTo(A123_INITIAL_BAL + depositAmount))
        assertThat(accountRepository.findById(A123_ID).getBalance(), equalTo(A123_INITIAL_BAL + depositAmount))
    }

    @Test
    fun testNonExistentAccount() {
        try {
            depositService.deposit(10.00, Z999_ID)
            fail("expected AccountNotFoundException")
        } catch (ex: AccountNotFoundException) {
        }
    }

    @Test
    fun testZeroDepositAmount() {
        try {
            depositService.deposit(0.00, C456_ID)
            fail("expected IllegalArgumentException")
        } catch (ex: IllegalArgumentException) {
        }
    }

    @Test
    fun testNegativeDepositAmount() {
        try {
            depositService.deposit(-10.00, C456_ID)
            fail("expected IllegalArgumentException")
        } catch (ex: IllegalArgumentException) {
        }
    }

    @Test
    fun testDepositAmountLessThanMinimum() {
        try {
            depositService.deposit(0.001, C456_ID)
            fail("expected IllegalArgumentException")
        } catch (ex: IllegalArgumentException) {
        }
    }

    @Test
    fun testCustomizedMinimumDepositAmount() {
        depositService.deposit(1.00, C456_ID)
        depositService.setMinimumDepositAmount(5.00)
        depositService.deposit(5.00, C456_ID)
        try {
            depositService.deposit(3.00, C456_ID)
            fail("expected IllegalArgumentException on 3.00 deposit that violates 5.00 minimum")
        } catch (ex: IllegalArgumentException) {
        }
    }
}
