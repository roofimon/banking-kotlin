package com.bank.djackatron2.adapter.outbound.persistence

import com.bank.djackatron2.application.port.outbound.AccountRepositoryPort
import com.bank.djackatron2.domain.Account
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException

@Repository
class JdbcAccountRepository(
    private val jdbcTemplate: JdbcTemplate
) : AccountRepositoryPort {

    override fun findById(srcAcctId: String): Account {
        return jdbcTemplate.queryForObject(
            "select id, balance from account where id = ?",
            AccountRowMapper(),
            srcAcctId
        )!!
    }

    override fun updateBalance(account: Account) {
        jdbcTemplate.update("update account set balance = ? where id = ?", account.getBalance(), account.getId())
    }

    companion object {
        private class AccountRowMapper : RowMapper<Account> {
            @Throws(SQLException::class)
            override fun mapRow(rs: ResultSet, rowNum: Int): Account {
                return Account(rs.getString("id"), rs.getDouble("balance"))
            }
        }
    }
}
