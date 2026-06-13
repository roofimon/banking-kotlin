package com.bank.djackatron2.adapter.inbound.web

import com.bank.djackatron2.adapter.inbound.web.dto.AccountEventDto
import com.bank.djackatron2.domain.InsufficientFundsException
import com.bank.djackatron2.domain.event.AccountCreditedEvent
import com.bank.djackatron2.port.inbound.DepositUseCase
import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.port.outbound.EventStorePort
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
) {

    @GetMapping("/{id}")
    fun handleById(@PathVariable("id") accId: String) =
        repository.findById(accId)

    @PostMapping("/{srcId}/transfer/{amount}/to/{destId}")
    @Throws(InsufficientFundsException::class)
    fun handleTransfer(
        @PathVariable("srcId") srcId: String,
        @PathVariable("amount") amount: Double,
        @PathVariable("destId") destId: String
    ) = transferUseCase.transfer(amount, srcId, destId)

    @PostMapping("/{id}/deposit/{amount}")
    fun handleDeposit(
        @PathVariable("id") accountId: String,
        @PathVariable("amount") amount: Double
    ) = depositUseCase.deposit(amount, accountId)

    @GetMapping("/{id}/history")
    fun history(@PathVariable("id") accountId: String): List<AccountEventDto> {
        repository.findById(accountId) // validates account exists; throws AccountNotFoundException if not
        return eventStore.eventsFor(accountId).reversed().map { event ->
            AccountEventDto(
                eventType = if (event is AccountCreditedEvent) "CREDITED" else "DEBITED",
                amount = event.amount,
                occurredAt = event.occurredAt.toString(),
            )
        }
    }
}
