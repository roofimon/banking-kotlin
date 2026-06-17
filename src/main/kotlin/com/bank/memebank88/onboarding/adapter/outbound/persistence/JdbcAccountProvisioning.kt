package com.bank.memebank88.onboarding.adapter.outbound.persistence

import com.bank.memebank88.onboarding.port.outbound.AccountProvisioningPort
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import kotlin.random.Random

/**
 * Creates a new ACCOUNT row with a generated account number of the form 2 letters + 3 digits
 * (e.g. "EF123"), fitting ACCOUNT.ID varchar(9). Retries on the rare id collision.
 */
@Repository
class JdbcAccountProvisioning(private val jdbcTemplate: JdbcTemplate) : AccountProvisioningPort {

    override fun createAccount(initialBalance: Double): String {
        repeat(MAX_ATTEMPTS) {
            val id = generateId()
            try {
                jdbcTemplate.update("insert into ACCOUNT (ID, BALANCE) values (?, ?)", id, initialBalance)
                return id
            } catch (_: DuplicateKeyException) {
                // id already taken — generate another and retry
            }
        }
        throw IllegalStateException("could not allocate a unique account number after $MAX_ATTEMPTS attempts")
    }

    private fun generateId(): String {
        val letters = (1..2).map { ('A'..'Z').random() }.joinToString("")
        val digits = Random.nextInt(0, 1000).toString().padStart(3, '0')
        return "$letters$digits"
    }

    companion object {
        private const val MAX_ATTEMPTS = 20
    }
}
