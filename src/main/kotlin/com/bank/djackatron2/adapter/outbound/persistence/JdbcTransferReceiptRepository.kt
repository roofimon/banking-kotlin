package com.bank.djackatron2.adapter.outbound.persistence

import com.bank.djackatron2.domain.TransferReceipt
import com.bank.djackatron2.port.outbound.StoredTransferReceipt
import com.bank.djackatron2.port.outbound.TransferReceiptRepositoryPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

@Repository
class JdbcTransferReceiptRepository(private val jdbcTemplate: JdbcTemplate) : TransferReceiptRepositoryPort {

    override fun save(receipt: TransferReceipt) {
        jdbcTemplate.update(
            """
            insert into TRANSFER_RECEIPT
                (TRANSFER_ID, SRC_ACCOUNT_ID, DST_ACCOUNT_ID, TRANSFER_AMOUNT, FEE_AMOUNT, SRC_FINAL_BALANCE, DST_FINAL_BALANCE, CREATED_AT)
            values (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            receipt.getTransferId(),
            receipt.getFinalSourceAccount().getId(),
            receipt.getFinalDestinationAccount().getId(),
            receipt.getTransferAmount(),
            receipt.getFeeAmount(),
            receipt.getFinalSourceAccount().getBalance(),
            receipt.getFinalDestinationAccount().getBalance(),
            Timestamp.from(Instant.now()),
        )
    }

    override fun findByAccountId(accountId: String): List<StoredTransferReceipt> =
        jdbcTemplate.query(
            """
            select TRANSFER_ID, SRC_ACCOUNT_ID, DST_ACCOUNT_ID, TRANSFER_AMOUNT, FEE_AMOUNT, SRC_FINAL_BALANCE, DST_FINAL_BALANCE, CREATED_AT
            from TRANSFER_RECEIPT
            where SRC_ACCOUNT_ID = ? or DST_ACCOUNT_ID = ?
            order by CREATED_AT desc, ID desc
            """.trimIndent(),
            StoredTransferReceiptRowMapper(),
            accountId,
            accountId,
        )

    override fun deleteAll() {
        jdbcTemplate.update("delete from TRANSFER_RECEIPT")
    }

    private class StoredTransferReceiptRowMapper : RowMapper<StoredTransferReceipt> {
        override fun mapRow(rs: ResultSet, rowNum: Int): StoredTransferReceipt =
            StoredTransferReceipt(
                transferId = rs.getString("TRANSFER_ID"),
                srcAccountId = rs.getString("SRC_ACCOUNT_ID"),
                dstAccountId = rs.getString("DST_ACCOUNT_ID"),
                transferAmount = rs.getDouble("TRANSFER_AMOUNT"),
                feeAmount = rs.getDouble("FEE_AMOUNT"),
                srcFinalBalance = rs.getDouble("SRC_FINAL_BALANCE"),
                dstFinalBalance = rs.getDouble("DST_FINAL_BALANCE"),
                createdAt = rs.getTimestamp("CREATED_AT").toInstant(),
            )
    }
}
