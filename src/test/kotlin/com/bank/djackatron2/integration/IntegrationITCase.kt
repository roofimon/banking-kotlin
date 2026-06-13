package com.bank.djackatron2.integration

import com.bank.djackatron2.adapter.outbound.persistence.JdbcAccountRepository
import com.bank.djackatron2.adapter.outbound.service.ZeroFeePolicy
import com.bank.djackatron2.application.usecase.DepositMoneyUseCase
import com.bank.djackatron2.application.usecase.TransferMoneyUseCase
import com.bank.djackatron2.domain.InsufficientFundsException
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
    @Throws(InsufficientFundsException::class)
    fun transferTenDollars() {
        val feePolicy = ZeroFeePolicy()
        val accountRepository = JdbcAccountRepository(JdbcTemplate(dataSource()))
        val transferService = TransferMoneyUseCase(accountRepository, feePolicy)

        assertThat(accountRepository.findById("A123").getBalance(), CoreMatchers.equalTo(100.00))
        assertThat(accountRepository.findById("C456").getBalance(), CoreMatchers.equalTo(0.00))

        transferService.transfer(10.00, "A123", "C456")

        assertThat(accountRepository.findById("A123").getBalance(), CoreMatchers.equalTo(90.00))
        assertThat(accountRepository.findById("C456").getBalance(), CoreMatchers.equalTo(10.00))
    }

    @Test
    fun depositTwentyDollars() {
        val accountRepository = JdbcAccountRepository(JdbcTemplate(dataSource()))
        val depositService = DepositMoneyUseCase(accountRepository)

        assertThat(accountRepository.findById("C456").getBalance(), CoreMatchers.equalTo(0.00))

        depositService.deposit(20.00, "C456")

        assertThat(accountRepository.findById("C456").getBalance(), CoreMatchers.equalTo(20.00))
    }

}

