package com.bank.memebank88.adapter.outbound.persistence

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

class JdbcAccountProvisioningTest {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var provisioning: JdbcAccountProvisioning

    @BeforeEach
    fun setUp() {
        val dataSource = EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .build()
        jdbcTemplate = JdbcTemplate(dataSource)
        provisioning = JdbcAccountProvisioning(jdbcTemplate)
    }

    @Test
    fun createsAccountWithTwoLettersThreeDigitsIdAndPersistsBalance() {
        val id = provisioning.createAccount(0.0)

        assertThat(id.matches(Regex("^[A-Z]{2}[0-9]{3}$")), equalTo(true))
        val balance = jdbcTemplate.queryForObject(
            "select BALANCE from ACCOUNT where ID = ?", Double::class.java, id,
        )
        assertThat(balance, equalTo(0.0))
    }

    @Test
    fun generatesDistinctIds() {
        val a = provisioning.createAccount(0.0)
        val b = provisioning.createAccount(0.0)
        assertThat(a == b, equalTo(false))
    }
}
