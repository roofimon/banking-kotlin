package com.bank.djackatron2.adapter.inbound.web

import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.domain.Account
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AccountControllerTest {

    private val repository: AccountRepositoryPort = mock(AccountRepositoryPort::class.java)
    private val transferUseCase: TransferUseCase = mock(TransferUseCase::class.java)
    private val controller: AccountController = AccountController(repository, transferUseCase)

    @Test
    fun testHandleById() {
        //given
        val accId = "A123"
        val account = Account(accId, 100.00)

        `when`(repository.findById(anyString())).thenReturn(account)

        //when
        val result = controller.handleById(accId)

        //then
        assertEquals(account, result)
    }

    @Test
    fun testHandleTransfer() {
        //given
        val srcId = "A123"
        val destId = "B123"

        //when
        controller.handleTransfer(srcId, 100.00, destId)

        //then
        verify(transferUseCase).transfer(100.00, srcId, destId)
    }

}