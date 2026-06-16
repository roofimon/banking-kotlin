package com.bank.memebank88.adapter.outbound.persistence

import com.bank.memebank88.domain.Account
import com.bank.memebank88.domain.TransferReceipt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

class JdbcTransferReceiptRepositoryTest {

    private lateinit var repository: JdbcTransferReceiptRepository

    @BeforeEach
    fun setUp() {
        val dataSource = EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .build()
        repository = JdbcTransferReceiptRepository(JdbcTemplate(dataSource))
    }

    private fun receipt(
        transferId: String, srcId: String, dstId: String,
        amount: Double, fee: Double, srcFinal: Double, dstFinal: Double,
    ) =
        TransferReceipt(transferId, Account(srcId, 0.0), Account(dstId, 0.0)).apply {
            setTransferAmount(amount)
            setFeeAmount(fee)
            setFinalSourceAccount(Account(srcId, srcFinal))
            setFinalDestinationAccount(Account(dstId, dstFinal))
        }

    @Test
    fun savesAndFindsByBothSourceAndDestination() {
        repository.save(receipt("tx-1", "A123", "C456", 50.00, 5.00, 45.00, 50.00))

        for (accountId in listOf("A123", "C456")) {
            val stored = repository.findByAccountId(accountId)
            assertThat(stored.size, equalTo(1))
            assertThat(stored[0].transferId, equalTo("tx-1"))
            assertThat(stored[0].srcAccountId, equalTo("A123"))
            assertThat(stored[0].dstAccountId, equalTo("C456"))
            assertThat(stored[0].transferAmount, equalTo(50.00))
            assertThat(stored[0].feeAmount, equalTo(5.00))
            assertThat(stored[0].srcFinalBalance, equalTo(45.00))
            assertThat(stored[0].dstFinalBalance, equalTo(50.00))
        }
    }

    @Test
    fun returnsEmptyForUnknownAccount() {
        assertThat(repository.findByAccountId("Z999").size, equalTo(0))
    }

    @Test
    fun deleteAllRemovesEverything() {
        repository.save(receipt("tx-2", "A123", "C456", 10.00, 0.00, 90.00, 10.00))
        repository.deleteAll()
        assertThat(repository.findByAccountId("A123").size, equalTo(0))
    }
}
