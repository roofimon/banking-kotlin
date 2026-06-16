package com.bank.memebank88.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.domain.Onboarding
import com.bank.memebank88.domain.OnboardingStatus
import com.bank.memebank88.port.outbound.OnboardingRepositoryPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

@Repository
class JdbcOnboardingRepository(private val jdbcTemplate: JdbcTemplate) : OnboardingRepositoryPort {

    override fun save(onboarding: Onboarding) {
        jdbcTemplate.update(
            """
            merge into ONBOARDING
                (ID, EMAIL, STATUS, EMAIL_CODE, NAME, PHONE, SESSION_TOKEN, CREDIT_SCORE, ACCOUNT_ID, CREATED_AT)
            key(ID) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            onboarding.id,
            onboarding.email,
            onboarding.status.name,
            onboarding.emailCode,
            onboarding.name,
            onboarding.phone,
            onboarding.sessionToken,
            onboarding.creditScore,
            onboarding.accountId,
            Timestamp.from(onboarding.createdAt),
        )
    }

    override fun findById(id: String): Option<Onboarding> =
        jdbcTemplate.query(
            """
            select ID, EMAIL, STATUS, EMAIL_CODE, NAME, PHONE, SESSION_TOKEN, CREDIT_SCORE, ACCOUNT_ID, CREATED_AT
            from ONBOARDING where ID = ?
            """.trimIndent(),
            OnboardingRowMapper(),
            id,
        ).firstOrNull().toOption()

    override fun deleteAll() {
        jdbcTemplate.update("delete from ONBOARDING")
    }

    private class OnboardingRowMapper : RowMapper<Onboarding> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Onboarding {
            val score = rs.getInt("CREDIT_SCORE").let { if (rs.wasNull()) null else it }
            return Onboarding(
                id = rs.getString("ID"),
                email = rs.getString("EMAIL"),
                status = OnboardingStatus.valueOf(rs.getString("STATUS")),
                emailCode = rs.getString("EMAIL_CODE"),
                name = rs.getString("NAME"),
                phone = rs.getString("PHONE"),
                sessionToken = rs.getString("SESSION_TOKEN"),
                creditScore = score,
                accountId = rs.getString("ACCOUNT_ID"),
                createdAt = rs.getTimestamp("CREATED_AT").toInstant(),
            )
        }
    }
}
