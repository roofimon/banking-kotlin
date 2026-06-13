package com.bank.djackatron2.adapter.inbound.web

import com.bank.djackatron2.domain.Account
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
        `when`(repository.findById(anyString())).thenReturn(account)

        val result = controller.handleById(accId)

        assertEquals(account, result)
    }

    @Test
    fun testHandleTransfer() {
        val srcId = "A123"
        val destId = "B123"

        controller.handleTransfer(srcId, 100.00, destId)

        verify(transferUseCase).transfer(100.00, srcId, destId)
    }

    @Test
    fun testHandleDeposit() {
        val accountId = "C456"
        val amount = 20.00

        controller.handleDeposit(accountId, amount)

        verify(depositUseCase).deposit(amount, accountId)
    }

    @Test
    fun testHandleHistory() {
        val accountId = "A123"
        val now = Instant.now()
        `when`(eventStore.eventsFor(accountId)).thenReturn(
            listOf(AccountCreditedEvent(accountId, 50.00, now))
        )

        val result = controller.history(accountId)

        assertEquals(1, result.size)
        assertEquals("CREDITED", result[0].eventType)
        assertEquals(50.00, result[0].amount)
    }
}
