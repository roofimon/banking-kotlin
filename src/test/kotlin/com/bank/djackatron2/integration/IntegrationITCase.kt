package com.bank.djackatron2.integration

import com.bank.djackatron2.adapter.outbound.persistence.EventSourcedAccountRepository
import com.bank.djackatron2.adapter.outbound.persistence.JdbcEventStore
import com.bank.djackatron2.adapter.outbound.service.ZeroFeePolicy
import com.bank.djackatron2.application.usecase.DepositMoneyUseCase
import com.bank.djackatron2.application.usecase.TransferMoneyUseCase
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import javax.sql.DataSource


class IntegrationITCase {

    @Bean
    fun dataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .addScript("classpath:test-data.sql").build()
    }

    @Test
    fun transferTenDollars() {
        val jdbcTemplate = JdbcTemplate(dataSource())
        val eventStore = JdbcEventStore(jdbcTemplate)
        val accountRepository = EventSourcedAccountRepository(jdbcTemplate, eventStore)
        val feePolicy = ZeroFeePolicy()
        val transferService = TransferMoneyUseCase(accountRepository, feePolicy, eventStore)

        assertThat(accountRepository.findById("A123").getOrNull()!!.getBalance(), CoreMatchers.equalTo(100.00))
        assertThat(accountRepository.findById("C456").getOrNull()!!.getBalance(), CoreMatchers.equalTo(0.00))

        transferService.transfer(10.00, "A123", "C456")

        assertThat(accountRepository.findById("A123").getOrNull()!!.getBalance(), CoreMatchers.equalTo(90.00))
        assertThat(accountRepository.findById("C456").getOrNull()!!.getBalance(), CoreMatchers.equalTo(10.00))
    }

    @Test
    fun depositTwentyDollars() {
        val jdbcTemplate = JdbcTemplate(dataSource())
        val eventStore = JdbcEventStore(jdbcTemplate)
        val accountRepository = EventSourcedAccountRepository(jdbcTemplate, eventStore)
        val depositService = DepositMoneyUseCase(accountRepository, eventStore)

        assertThat(accountRepository.findById("C456").getOrNull()!!.getBalance(), CoreMatchers.equalTo(0.00))

        depositService.deposit(20.00, "C456")

        assertThat(accountRepository.findById("C456").getOrNull()!!.getBalance(), CoreMatchers.equalTo(20.00))

        val events = eventStore.eventsFor("C456")
        assertThat(events.size, CoreMatchers.equalTo(1))
        assertThat(events[0].amount, CoreMatchers.equalTo(20.00))
    }

    @Test
    fun historyReturnsMostRecentFirst() {
        val jdbcTemplate = JdbcTemplate(dataSource())
        val eventStore = JdbcEventStore(jdbcTemplate)
        val accountRepository = EventSourcedAccountRepository(jdbcTemplate, eventStore)
        val depositService = DepositMoneyUseCase(accountRepository, eventStore)

        depositService.deposit(10.00, "C456")
        depositService.deposit(5.00, "C456")

        val events = eventStore.eventsFor("C456")
        assertThat(events.size, CoreMatchers.equalTo(2))
        assertThat(accountRepository.findById("C456").getOrNull()!!.getBalance(), CoreMatchers.equalTo(15.00))
    }
}
