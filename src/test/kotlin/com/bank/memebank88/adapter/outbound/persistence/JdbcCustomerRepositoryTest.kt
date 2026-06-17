package com.bank.memebank88.onboarding.adapter.outbound.persistence

import com.bank.memebank88.onboarding.domain.Customer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import java.time.Instant

class JdbcCustomerRepositoryTest {

    private lateinit var repository: JdbcCustomerRepository

    @BeforeEach
    fun setUp() {
        val dataSource = EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .build()
        repository = JdbcCustomerRepository(JdbcTemplate(dataSource))
    }

    @Test
    fun savesAndFindsByAccountId() {
        val customer = Customer("AC0000001", "jane@example.com", "Jane Doe", "0812345678", "Ab3Cd", 720, Instant.now())

        repository.save(customer)

        val found = repository.findByAccountId("AC0000001").getOrNull()!!
        assertThat(found.email, equalTo("jane@example.com"))
        assertThat(found.name, equalTo("Jane Doe"))
        assertThat(found.phone, equalTo("0812345678"))
        assertThat(found.password, equalTo("Ab3Cd"))
        assertThat(found.creditScore, equalTo(720))
    }

    @Test
    fun saveUpsertsExistingRow() {
        repository.save(Customer("AC0000002", "a@b.com", "Old Name", "0800000000", "Old12", 600, Instant.now()))
        repository.save(Customer("AC0000002", "a@b.com", "New Name", "0811111111", "New34", 650, Instant.now()))

        val found = repository.findByAccountId("AC0000002").getOrNull()!!
        assertThat(found.name, equalTo("New Name"))
        assertThat(found.creditScore, equalTo(650))
    }

    @Test
    fun missingAccountReturnsNone() {
        assertThat(repository.findByAccountId("NOPE").getOrNull(), nullValue())
    }

    @Test
    fun findsByEmail() {
        repository.save(Customer("AC0000003", "login@example.com", "Log In", "0822222222", "Pw345", 700, Instant.now()))

        val found = repository.findByEmail("login@example.com").getOrNull()!!
        assertThat(found.accountId, equalTo("AC0000003"))
        assertThat(found.password, equalTo("Pw345"))
    }

    @Test
    fun missingEmailReturnsNone() {
        assertThat(repository.findByEmail("nobody@example.com").getOrNull(), nullValue())
    }
}
