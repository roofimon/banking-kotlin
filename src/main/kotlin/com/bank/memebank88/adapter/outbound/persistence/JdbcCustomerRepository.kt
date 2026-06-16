package com.bank.memebank88.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.domain.Customer
import com.bank.memebank88.port.outbound.CustomerRepositoryPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

@Repository
class JdbcCustomerRepository(private val jdbcTemplate: JdbcTemplate) : CustomerRepositoryPort {

    override fun save(customer: Customer) {
        jdbcTemplate.update(
            """
            merge into CUSTOMER
                (ACCOUNT_ID, EMAIL, NAME, PHONE, PASSWORD, CREDIT_SCORE, CREATED_AT)
            key(ACCOUNT_ID) values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            customer.accountId,
            customer.email,
            customer.name,
            customer.phone,
            customer.password,
            customer.creditScore,
            Timestamp.from(customer.createdAt),
        )
    }

    override fun findByAccountId(accountId: String): Option<Customer> =
        jdbcTemplate.query(
            """
            select ACCOUNT_ID, EMAIL, NAME, PHONE, PASSWORD, CREDIT_SCORE, CREATED_AT
            from CUSTOMER where ACCOUNT_ID = ?
            """.trimIndent(),
            CustomerRowMapper(),
            accountId,
        ).firstOrNull().toOption()

    override fun findByEmail(email: String): Option<Customer> =
        jdbcTemplate.query(
            """
            select ACCOUNT_ID, EMAIL, NAME, PHONE, PASSWORD, CREDIT_SCORE, CREATED_AT
            from CUSTOMER where EMAIL = ?
            """.trimIndent(),
            CustomerRowMapper(),
            email,
        ).firstOrNull().toOption()

    override fun deleteAll() {
        jdbcTemplate.update("delete from CUSTOMER")
    }

    private class CustomerRowMapper : RowMapper<Customer> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Customer =
            Customer(
                accountId = rs.getString("ACCOUNT_ID"),
                email = rs.getString("EMAIL"),
                name = rs.getString("NAME"),
                phone = rs.getString("PHONE"),
                password = rs.getString("PASSWORD"),
                creditScore = rs.getInt("CREDIT_SCORE"),
                createdAt = rs.getTimestamp("CREATED_AT").toInstant(),
            )
    }
}
