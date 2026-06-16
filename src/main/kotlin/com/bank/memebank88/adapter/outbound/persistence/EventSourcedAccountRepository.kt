package com.bank.memebank88.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.domain.Account
import com.bank.memebank88.domain.event.AccountCreditedEvent
import com.bank.memebank88.domain.event.AccountDebitedEvent
import com.bank.memebank88.port.outbound.AccountRepositoryPort
import com.bank.memebank88.port.outbound.EventStorePort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class EventSourcedAccountRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val eventStore: EventStorePort,
) : AccountRepositoryPort {

    override fun findById(srcAcctId: String): Option<Account> =
        jdbcTemplate.query(
            "select ID, BALANCE from ACCOUNT where ID = ?",
            SeedRowMapper(),
            srcAcctId
        ).firstOrNull().toOption().map { row ->
            val currentBalance = eventStore.eventsFor(srcAcctId).fold(row.second) { balance, event ->
                when (event) {
                    is AccountCreditedEvent -> balance + event.amount
                    is AccountDebitedEvent -> balance - event.amount
                }
            }
            Account(row.first, currentBalance)
        }

    private class SeedRowMapper : RowMapper<Pair<String, Double>> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Pair<String, Double> =
            Pair(rs.getString("ID"), rs.getDouble("BALANCE"))
    }
}
