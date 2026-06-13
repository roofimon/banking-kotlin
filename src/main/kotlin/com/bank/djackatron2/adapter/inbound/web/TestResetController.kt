package com.bank.djackatron2.adapter.inbound.web

import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("!prod")
class TestResetController(private val jdbcTemplate: JdbcTemplate) {

    @PostMapping("/test/reset")
    fun reset() {
        jdbcTemplate.execute("DELETE FROM ACCOUNT")
        jdbcTemplate.update("INSERT INTO ACCOUNT (ID, BALANCE) VALUES ('A123', 100.00)")
        jdbcTemplate.update("INSERT INTO ACCOUNT (ID, BALANCE) VALUES ('C456', 0.00)")
    }
}
