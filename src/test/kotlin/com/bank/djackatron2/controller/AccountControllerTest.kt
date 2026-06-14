package com.bank.djackatron2.adapter.inbound.web

import arrow.core.None
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import com.bank.djackatron2.adapter.inbound.web.dto.AccountEventDto
import com.bank.djackatron2.adapter.inbound.web.dto.ErrorResponse
import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.DepositReceipt
import com.bank.djackatron2.domain.DomainError
import com.bank.djackatron2.domain.event.AccountCreditedEvent
import com.bank.djackatron2.port.inbound.DepositUseCase
import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.port.outbound.EventStorePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.time.Instant

class AccountControllerTest {

    private val repository: AccountRepositoryPort = mock(AccountRepositoryPort::class.java)
    private val transferUseCase: TransferUseCase = mock(TransferUseCase::class.java)
    private val depositUseCase: DepositUseCase = mock(DepositUseCase::class.java)
    private val eventStore: EventStorePort = mock(EventStorePort::class.java)
    private val controller: AccountController = AccountController(repository, transferUseCase, depositUseCase, eventStore)

    @Test
    fun testHandleById() {
        val accId = "A123"
        val account = Account(accId, 100.00)
        `when`(repository.findById(anyString())).thenReturn(Some(account))

        val result = controller.handleById(accId)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(account, result.body)
    }

    @Test
    fun testHandleByIdNotFound() {
        val accId = "Z999"
        `when`(repository.findById(anyString())).thenReturn(None)

        val result = controller.handleById(accId)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        assertEquals("ACCOUNT_NOT_FOUND", (result.body as ErrorResponse).code)
    }

    @Test
    fun testHandleTransfer() {
        val srcId = "A123"
        val destId = "B123"
        `when`(transferUseCase.transfer(100.00, srcId, destId)).thenReturn(Unit.right())

        val result = controller.handleTransfer(srcId, 100.00, destId)

        assertEquals(HttpStatus.ACCEPTED, result.statusCode)
        verify(transferUseCase).transfer(100.00, srcId, destId)
    }

    @Test
    fun testHandleTransferInsufficientFunds() {
        val srcId = "A123"
        val destId = "B123"
        `when`(transferUseCase.transfer(200.00, srcId, destId))
            .thenReturn(DomainError.InsufficientFunds(srcId, 200.00, 100.00).left())

        val result = controller.handleTransfer(srcId, 200.00, destId)

        assertEquals(HttpStatus.CONFLICT, result.statusCode)
        assertEquals("INSUFFICIENT_FUNDS", (result.body as ErrorResponse).code)
    }

    @Test
    fun testHandleDeposit() {
        val accountId = "C456"
        val amount = 20.00
        val account = Account(accountId, 20.00)
        val receipt = DepositReceipt(amount, Account(accountId, 0.00), account)
        `when`(depositUseCase.deposit(amount, accountId)).thenReturn(receipt.right())

        val result = controller.handleDeposit(accountId, amount)

        assertEquals(HttpStatus.OK, result.statusCode)
        verify(depositUseCase).deposit(amount, accountId)
    }

    @Test
    fun testHandleDepositBelowMinimum() {
        val accountId = "C456"
        val amount = 0.001
        `when`(depositUseCase.deposit(amount, accountId))
            .thenReturn(DomainError.BelowMinimum(amount, 0.01, "deposit").left())

        val result = controller.handleDeposit(accountId, amount)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
        assertEquals("BELOW_MINIMUM", (result.body as ErrorResponse).code)
    }

    @Test
    fun testHandleHistory() {
        val accountId = "A123"
        val now = Instant.now()
        `when`(repository.findById(accountId)).thenReturn(Some(Account(accountId, 50.00)))
        `when`(eventStore.eventsFor(accountId)).thenReturn(
            listOf(AccountCreditedEvent(accountId, 50.00, now))
        )

        val result = controller.history(accountId)

        assertEquals(HttpStatus.OK, result.statusCode)
        @Suppress("UNCHECKED_CAST")
        val rows = result.body as List<AccountEventDto>
        assertEquals(1, rows.size)
        assertEquals("CREDITED", rows[0].eventType)
        assertEquals(50.00, rows[0].amount)
    }

    @Test
    fun testHandleHistoryNotFound() {
        val accountId = "Z999"
        `when`(repository.findById(accountId)).thenReturn(None)

        val result = controller.history(accountId)

        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        assertEquals("ACCOUNT_NOT_FOUND", (result.body as ErrorResponse).code)
    }
}
