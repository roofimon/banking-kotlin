package com.bank.djackatron2.adapter.inbound.web

import com.bank.djackatron2.port.inbound.TransferUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.domain.InsufficientFundsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account")
class AccountController(
    private val repository: AccountRepositoryPort,
    private val transferUseCase: TransferUseCase
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
}
