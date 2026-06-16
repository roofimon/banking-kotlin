package com.bank.memebank88.adapter.outbound.persistence

import com.bank.memebank88.domain.event.AccountCreditedEvent
import com.bank.memebank88.domain.event.AccountDebitedEvent
import com.bank.memebank88.domain.event.AccountEvent
import com.bank.memebank88.port.outbound.EventStorePort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

@Repository
class JdbcEventStore(private val jdbcTemplate: JdbcTemplate) : EventStorePort {

    override fun append(event: AccountEvent) {
        val eventType = when (event) {
            is AccountCreditedEvent -> "CREDITED"
            is AccountDebitedEvent -> "DEBITED"
        }
        jdbcTemplate.update(
            "insert into ACCOUNT_EVENT (ACCOUNT_ID, EVENT_TYPE, AMOUNT, OCCURRED_AT) values (?, ?, ?, ?)",
            event.accountId, eventType, event.amount, Timestamp.from(event.occurredAt)
        )
    }

    override fun eventsFor(accountId: String): List<AccountEvent> =
        jdbcTemplate.query(
            "select ACCOUNT_ID, EVENT_TYPE, AMOUNT, OCCURRED_AT from ACCOUNT_EVENT where ACCOUNT_ID = ? order by OCCURRED_AT",
            AccountEventRowMapper(),
            accountId
        )

    override fun clearAll() {
        jdbcTemplate.update("delete from ACCOUNT_EVENT")
    }

    private class AccountEventRowMapper : RowMapper<AccountEvent> {
        override fun mapRow(rs: ResultSet, rowNum: Int): AccountEvent {
            val accountId = rs.getString("ACCOUNT_ID")
            val amount = rs.getDouble("AMOUNT")
            val occurredAt = rs.getTimestamp("OCCURRED_AT").toInstant()
            return when (rs.getString("EVENT_TYPE")) {
                "CREDITED" -> AccountCreditedEvent(accountId, amount, occurredAt)
                else -> AccountDebitedEvent(accountId, amount, occurredAt)
            }
        }
    }
}
