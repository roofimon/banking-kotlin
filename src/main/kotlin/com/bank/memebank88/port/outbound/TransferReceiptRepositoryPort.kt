package com.bank.memebank88.port.outbound

import com.bank.memebank88.domain.TransferReceipt
import java.time.Instant

/**
 * Outbound port for persisting completed transfer receipts and reading them back.
 */
interface TransferReceiptRepositoryPort {
    /** Persists a completed transfer's [receipt]. */
    fun save(receipt: TransferReceipt)

    /** Returns receipts where [accountId] is the source or the destination, newest first. */
    fun findByAccountId(accountId: String): List<StoredTransferReceipt>

    /** Removes all persisted receipts (test-reset support). */
    fun deleteAll()
}

/** A persisted transfer receipt as read back from storage. */
data class StoredTransferReceipt(
    val transferId: String,
    val srcAccountId: String,
    val dstAccountId: String,
    val transferAmount: Double,
    val feeAmount: Double,
    val srcFinalBalance: Double,
    val dstFinalBalance: Double,
    val createdAt: Instant,
)
