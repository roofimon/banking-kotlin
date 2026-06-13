package com.bank.djackatron2.application.usecase

import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.DepositReceipt
import com.bank.djackatron2.port.inbound.DepositUseCase
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class DepositMoneyUseCase(
    private val accountRepository: AccountRepositoryPort,
) : DepositUseCase {

    private var minimumDepositAmount = 0.01

    override fun deposit(amount: Double, accountId: String): DepositReceipt {
        if (amount < minimumDepositAmount) {
            throw IllegalArgumentException("deposit amount must be at least $minimumDepositAmount")
        }

        val account = accountRepository.findById(accountId)
        val initial = Account.copy(account)

        account.credit(amount)
        accountRepository.updateBalance(account)

        return DepositReceipt(amount, initial, Account.copy(account))
    }

    override fun setMinimumDepositAmount(minimumDepositAmount: Double) {
        this.minimumDepositAmount = minimumDepositAmount
    }
}
