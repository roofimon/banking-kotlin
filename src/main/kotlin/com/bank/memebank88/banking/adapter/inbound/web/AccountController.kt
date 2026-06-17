package com.bank.memebank88.banking.adapter.inbound.web

import com.bank.memebank88.banking.adapter.inbound.web.dto.AccountEventDto
import com.bank.memebank88.banking.adapter.inbound.web.dto.TransferAcceptedResponse
import com.bank.memebank88.banking.adapter.inbound.web.dto.TransferReceiptDto
import com.bank.memebank88.banking.domain.BankingError
import com.bank.memebank88.banking.domain.event.AccountCreditedEvent
import com.bank.memebank88.banking.port.inbound.DepositUseCase
import com.bank.memebank88.banking.port.inbound.TransferCommand
import com.bank.memebank88.banking.port.inbound.TransferUseCase
import com.bank.memebank88.banking.port.outbound.AccountRepositoryPort
import com.bank.memebank88.banking.port.outbound.EventStorePort
import com.bank.memebank88.banking.port.outbound.StoredTransferReceipt
import com.bank.memebank88.banking.port.outbound.TransferReceiptRepositoryPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account")
class AccountController(
    private val repository: AccountRepositoryPort,
    private val transferUseCase: TransferUseCase,
    private val depositUseCase: DepositUseCase,
    private val eventStore: EventStorePort,
    private val receiptRepository: TransferReceiptRepositoryPort,
) {

    @GetMapping("/{id}")
    fun handleById(@PathVariable("id") accId: String): ResponseEntity<*> =
        repository.findById(accId).fold(
            { BankingError.AccountNotFound(accId).toResponse() },
            { ResponseEntity.ok(it) },
        )

    @PostMapping("/{srcId}/transfer/{amount}/to/{destId}")
    fun handleTransfer(
        @PathVariable("srcId") srcId: String,
        @PathVariable("amount") amount: Double,
        @PathVariable("destId") destId: String
    ): ResponseEntity<*> =
        transferUseCase.transfer(TransferCommand(amount, srcId, destId)).fold(
            { it.toResponse() },
            { ResponseEntity.accepted().body(TransferAcceptedResponse(it.value, "Transfer submitted; your receipt is being sent.")) },
        )

    @PostMapping("/{id}/deposit/{amount}")
    fun handleDeposit(
        @PathVariable("id") accountId: String,
        @PathVariable("amount") amount: Double
    ): ResponseEntity<*> =
        depositUseCase.deposit(amount, accountId).fold({ it.toResponse() }, { ResponseEntity.ok(it) })

    @GetMapping("/{id}/history")
    fun history(@PathVariable("id") accountId: String): ResponseEntity<*> =
        repository.findById(accountId).fold(
            { BankingError.AccountNotFound(accountId).toResponse() },
            {
                val rows = eventStore.eventsFor(accountId).reversed().map { event ->
                    AccountEventDto(
                        eventType = if (event is AccountCreditedEvent) "CREDITED" else "DEBITED",
                        amount = event.amount,
                        occurredAt = event.occurredAt.toString(),
                    )
                }
                ResponseEntity.ok(rows)
            },
        )

    @GetMapping("/{id}/receipts")
    fun receipts(@PathVariable("id") accountId: String): ResponseEntity<*> =
        repository.findById(accountId).fold(
            { BankingError.AccountNotFound(accountId).toResponse() },
            { ResponseEntity.ok(receiptRepository.findByAccountId(accountId).map { it.toDto() }) },
        )

    private fun StoredTransferReceipt.toDto() = TransferReceiptDto(
        transferId = transferId,
        srcAccountId = srcAccountId,
        dstAccountId = dstAccountId,
        transferAmount = transferAmount,
        feeAmount = feeAmount,
        srcFinalBalance = srcFinalBalance,
        dstFinalBalance = dstFinalBalance,
        createdAt = createdAt.toString(),
    )
}
