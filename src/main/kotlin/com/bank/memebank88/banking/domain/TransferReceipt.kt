package com.bank.memebank88.banking.domain

class TransferReceipt(
    private val transferId: String,
    private var initialSourceAccountCopy: Account,
    private var initialDestinationAccountCopy: Account,
) {

    private var transferAmount: Double = 0.0
    private var feeAmount: Double = 0.0
    private lateinit var finalSourceAccountCopy: Account
    private lateinit var finalDestinationAccountCopy: Account

    fun setTransferAmount(transferAmount: Double) {
        this.transferAmount = transferAmount
    }

    fun setFeeAmount(feeAmount: Double) {
        this.feeAmount = feeAmount
    }

    fun setFinalSourceAccount(finalSourceAccountCopy: Account) {
        this.finalSourceAccountCopy = finalSourceAccountCopy
    }

    fun setFinalDestinationAccount(finalDestinationAccountCopy: Account) {
        this.finalDestinationAccountCopy = finalDestinationAccountCopy
    }

    fun getTransferId() = transferId
    fun getTransferAmount() = transferAmount
    fun getFeeAmount() = feeAmount
    fun getFinalSourceAccount() = finalSourceAccountCopy
    fun getFinalDestinationAccount() = finalDestinationAccountCopy

    override fun toString(): String =
        """
            Transfer $transferId: transferred $transferAmount from account ${initialSourceAccountCopy.getId()} to ${initialDestinationAccountCopy.getId()}, with fee amount: $feeAmount
                initial balance for account ${initialSourceAccountCopy.getId()}: ${initialSourceAccountCopy.getBalance()}; new balance: ${finalSourceAccountCopy.getBalance()}
                initial balance for account ${initialDestinationAccountCopy.getId()}: ${initialDestinationAccountCopy.getBalance()}; new balance: ${finalDestinationAccountCopy.getBalance()}
        """.trimIndent()


}
