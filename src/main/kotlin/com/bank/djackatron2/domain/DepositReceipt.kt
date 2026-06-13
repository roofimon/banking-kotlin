package com.bank.djackatron2.domain

class DepositReceipt(
    private val depositAmount: Double,
    private val initialAccount: Account,
    private val finalAccount: Account,
) {
    fun getDepositAmount() = depositAmount
    fun getInitialAccount() = initialAccount
    fun getFinalAccount() = finalAccount

    override fun toString(): String =
        """
            Deposited $depositAmount to account ${finalAccount.getId()};
            initial balance: ${initialAccount.getBalance()}; new balance: ${finalAccount.getBalance()}
        """.trimIndent()
}
