package com.bank.djackatron2.application.usecase

import arrow.core.Some
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.A123_ID
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.A123_INITIAL_BAL
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.C456_ID
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.C456_INITIAL_BAL
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventSourcedAccountRepository.Companion.Z999_ID
import com.bank.djackatron2.adapter.outbound.persistence.InMemoryEventStore
import com.bank.djackatron2.adapter.outbound.service.FlatFeePolicy
import com.bank.djackatron2.adapter.outbound.service.ZeroFeePolicy
import com.bank.djackatron2.application.event.TransferCompletedEvent
import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.DomainError
import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.inbound.TransferCommand
import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.port.outbound.EventStorePort
import com.bank.djackatron2.port.outbound.FeePolicyPort
import com.bank.djackatron2.port.outbound.TimeServicePort
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalTime

@TestInstance(PER_CLASS)
class DefaultTransferServiceTest {

    private lateinit var eventStore: InMemoryEventStore
    private lateinit var accountRepository: AccountRepositoryPort
    private lateinit var transferService: TransferUseCase

    // Capturing publisher: the receipt is no longer returned, it is published to the bus.
    private lateinit var publishedEvents: MutableList<Any>
    private lateinit var eventPublisher: ApplicationEventPublisher

    private fun lastReceipt(): TransferReceipt =
        (publishedEvents.last() as TransferCompletedEvent).receipt

    @BeforeEach
    fun setUp() {
        eventStore = InMemoryEventStore()
        accountRepository = InMemoryEventSourcedAccountRepository(
            mapOf(A123_ID to A123_INITIAL_BAL, C456_ID to C456_INITIAL_BAL),
            eventStore
        )
        publishedEvents = mutableListOf()
        eventPublisher = ApplicationEventPublisher { event -> publishedEvents.add(event) }
        val feePolicy = ZeroFeePolicy()
        transferService = TransferMoneyUseCase(accountRepository, feePolicy, eventStore, eventPublisher)

        assertThat(accountRepository.findById(A123_ID).getOrNull()!!.getBalance(), CoreMatchers.equalTo(A123_INITIAL_BAL))
        assertThat(accountRepository.findById(C456_ID).getOrNull()!!.getBalance(), CoreMatchers.equalTo(C456_INITIAL_BAL))
    }

    @Test
    fun testTransfer() {
        val transferAmount = 100.00

        val result = transferService.transfer(TransferCommand(transferAmount, A123_ID, C456_ID))
        assertThat(result.isRight(), CoreMatchers.equalTo(true))
        assertThat(result.getOrNull()!!.value.isNotBlank(), CoreMatchers.equalTo(true))

        val receipt = lastReceipt()
        // The receipt carries the same id returned to the caller.
        assertThat(receipt.getTransferId(), CoreMatchers.equalTo(result.getOrNull()!!.value))
        assertThat(receipt.getTransferAmount(), CoreMatchers.equalTo(transferAmount))
        assertThat(
            receipt.getFinalSourceAccount().getBalance(),
            CoreMatchers.equalTo(A123_INITIAL_BAL - transferAmount)
        )
        assertThat(
            receipt.getFinalDestinationAccount().getBalance(),
            CoreMatchers.equalTo(C456_INITIAL_BAL + transferAmount)
        )

        assertThat(
            accountRepository.findById(A123_ID).getOrNull()!!.getBalance(),
            CoreMatchers.equalTo(A123_INITIAL_BAL - transferAmount)
        )
        assertThat(
            accountRepository.findById(C456_ID).getOrNull()!!.getBalance(),
            CoreMatchers.equalTo(C456_INITIAL_BAL + transferAmount)
        )
    }

    @Test
    fun testTransferUsingDynamicStub() {
        val transferAmount = 100.00
        val srcAccId = "A123"
        val srcAcc = Account(srcAccId, 100.00)
        val desAccId = "C456"
        val desAcc = Account(desAccId, 0.00)

        val mockAccRepo: AccountRepositoryPort = mock(AccountRepositoryPort::class.java)
        `when`(mockAccRepo.findById(srcAccId)).thenReturn(Some(srcAcc))
        `when`(mockAccRepo.findById(desAccId)).thenReturn(Some(desAcc))

        val mockFeePolicy = mock(FeePolicyPort::class.java)
        `when`(mockFeePolicy.calculateFee(anyDouble())).thenReturn(0.00)

        val mockEventStore: EventStorePort = mock(EventStorePort::class.java)
        val captured = mutableListOf<Any>()
        val publisher = ApplicationEventPublisher { event -> captured.add(event) }
        val transferService: TransferUseCase = TransferMoneyUseCase(mockAccRepo, mockFeePolicy, mockEventStore, publisher)

        val result = transferService.transfer(TransferCommand(transferAmount, srcAccId, desAccId))
        assertThat(result.isRight(), CoreMatchers.equalTo(true))

        val receipt = (captured.last() as TransferCompletedEvent).receipt
        assertThat(receipt.getTransferAmount(), CoreMatchers.equalTo(transferAmount))
        assertThat(receipt.getFinalSourceAccount().getBalance(), CoreMatchers.equalTo(0.00))
        assertThat(receipt.getFinalDestinationAccount().getBalance(), CoreMatchers.equalTo(100.00))
    }

    @Test
    fun testTransferWithCheckingTimeService() {
        val transferAmount = 100.00
        val mockTimeService = mock(TimeServicePort::class.java)
        `when`(mockTimeService.isServiceAvailable(any<LocalTime>())).thenReturn(true)
        transferService.setTimeService(mockTimeService)

        val result = transferService.transfer(TransferCommand(transferAmount, A123_ID, C456_ID))
        assertThat(result.isRight(), CoreMatchers.equalTo(true))

        val receipt = lastReceipt()
        assertThat(receipt.getTransferAmount(), CoreMatchers.equalTo(transferAmount))
        assertThat(
            receipt.getFinalSourceAccount().getBalance(),
            CoreMatchers.equalTo(A123_INITIAL_BAL - transferAmount)
        )
        assertThat(
            receipt.getFinalDestinationAccount().getBalance(),
            CoreMatchers.equalTo(C456_INITIAL_BAL + transferAmount)
        )

        assertThat(
            accountRepository.findById(A123_ID).getOrNull()!!.getBalance(),
            CoreMatchers.equalTo(A123_INITIAL_BAL - transferAmount)
        )
        assertThat(
            accountRepository.findById(C456_ID).getOrNull()!!.getBalance(),
            CoreMatchers.equalTo(C456_INITIAL_BAL + transferAmount)
        )
        verify(mockTimeService).isServiceAvailable(any<LocalTime>())
    }

    @Test
    fun testTransferWithCheckingOutofTimeService() {
        val transferAmount = 100.00
        val mockTimeService = mock(TimeServicePort::class.java)
        `when`(mockTimeService.isServiceAvailable(any<LocalTime>())).thenReturn(false)
        transferService.setTimeService(mockTimeService)

        val error = transferService.transfer(TransferCommand(transferAmount, A123_ID, C456_ID)).leftOrNull()

        assertThat(error, CoreMatchers.instanceOf(DomainError.OutOfService::class.java))
        verify(mockTimeService).isServiceAvailable(any<LocalTime>())
    }

    @Test
    fun testInsufficientFunds() {
        val overage = 9.00
        val transferAmount = A123_INITIAL_BAL + overage

        val error = transferService.transfer(TransferCommand(transferAmount, A123_ID, C456_ID)).leftOrNull()

        assertThat(error, CoreMatchers.instanceOf(DomainError.InsufficientFunds::class.java))
    }

    @Test
    fun testNonExistentSourceAccount() {
        val error = transferService.transfer(TransferCommand(1.00, Z999_ID, C456_ID)).leftOrNull()
        assertThat(error, CoreMatchers.instanceOf(DomainError.AccountNotFound::class.java))

        assertThat(accountRepository.findById(C456_ID).getOrNull()!!.getBalance(), CoreMatchers.equalTo(C456_INITIAL_BAL))
    }

    @Test
    fun testNonExistentDestinationAccount() {
        val error = transferService.transfer(TransferCommand(1.00, A123_ID, Z999_ID)).leftOrNull()
        assertThat(error, CoreMatchers.instanceOf(DomainError.AccountNotFound::class.java))

        assertThat(accountRepository.findById(A123_ID).getOrNull()!!.getBalance(), CoreMatchers.equalTo(A123_INITIAL_BAL))
    }

    @Test
    fun testZeroTransferAmount() {
        val error = transferService.transfer(TransferCommand(0.00, A123_ID, C456_ID)).leftOrNull()
        assertThat(error, CoreMatchers.instanceOf(DomainError.BelowMinimum::class.java))
    }

    @Test
    fun testNegativeTransferAmount() {
        val error = transferService.transfer(TransferCommand(-100.00, A123_ID, C456_ID)).leftOrNull()
        assertThat(error, CoreMatchers.instanceOf(DomainError.BelowMinimum::class.java))
    }

    @Test
    fun testTransferAmountLessThanOneCent() {
        val error = transferService.transfer(TransferCommand(0.009, A123_ID, C456_ID)).leftOrNull()
        assertThat(error, CoreMatchers.instanceOf(DomainError.BelowMinimum::class.java))
    }

    @Test
    fun testCustomizedMinimumTransferAmount() {
        transferService.transfer(TransferCommand(1.00, A123_ID, C456_ID))
        transferService.setMinimumTransferAmount(10.00)
        transferService.transfer(TransferCommand(10.00, A123_ID, C456_ID))

        val error = transferService.transfer(TransferCommand(9.00, A123_ID, C456_ID)).leftOrNull()
        assertThat(
            "expected BelowMinimum on 9.00 transfer that violates 10.00 minimum",
            error, CoreMatchers.instanceOf(DomainError.BelowMinimum::class.java)
        )
    }

    @Test
    fun testNonZeroFeePolicy() {
        val flatFee = 5.00
        val transferAmount = 95.00
        transferService = TransferMoneyUseCase(accountRepository, FlatFeePolicy(flatFee), eventStore, eventPublisher)
        transferService.transfer(TransferCommand(transferAmount, A123_ID, C456_ID))
        assertThat(
            accountRepository.findById(A123_ID).getOrNull()!!.getBalance(),
            CoreMatchers.equalTo(A123_INITIAL_BAL - transferAmount - flatFee)
        )
        assertThat(
            accountRepository.findById(C456_ID).getOrNull()!!.getBalance(),
            CoreMatchers.equalTo(C456_INITIAL_BAL + transferAmount)
        )
    }

    @Test
    fun testMaximumTransferWithFlatFeePolicy() {
        val flatFee = 5.00
        val transferAmount = 99.00
        transferService = TransferMoneyUseCase(accountRepository, FlatFeePolicy(flatFee), eventStore, eventPublisher)

        val error = transferService.transfer(TransferCommand(transferAmount, A123_ID, C456_ID)).leftOrNull()
        assertThat(error, CoreMatchers.instanceOf(DomainError.InsufficientFunds::class.java))
    }
}
