package com.bank.djackatron2.adapter.outbound.persistence

import com.bank.djackatron2.domain.Account
import com.bank.djackatron2.domain.event.AccountCreditedEvent
import com.bank.djackatron2.domain.event.AccountDebitedEvent
import com.bank.djackatron2.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.port.outbound.EventStorePort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import javax.security.auth.login.AccountNotFoundException

@Repository
class EventSourcedAccountRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val eventStore: EventStorePort,
) : AccountRepositoryPort {

    override fun findById(srcAcctId: String): Account {
        val row = jdbcTemplate.query(
            "select ID, BALANCE from ACCOUNT where ID = ?",
            SeedRowMapper(),
            srcAcctId
        ).firstOrNull() ?: throw AccountNotFoundException(srcAcctId)

        val currentBalance = eventStore.eventsFor(srcAcctId).fold(row.second) { balance, event ->
            when (event) {
                is AccountCreditedEvent -> balance + event.amount
                is AccountDebitedEvent -> balance - event.amount
            }
        }
        return Account(row.first, currentBalance)
    }

    private class SeedRowMapper : RowMapper<Pair<String, Double>> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Pair<String, Double> =
            Pair(rs.getString("ID"), rs.getDouble("BALANCE"))
    }
}
