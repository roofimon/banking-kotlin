package com.bank.memebank88.onboarding.adapter.outbound.persistence

import com.bank.memebank88.onboarding.domain.Onboarding
import com.bank.memebank88.onboarding.domain.OnboardingStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import java.time.Instant

class JdbcOnboardingRepositoryTest {

    private lateinit var repository: JdbcOnboardingRepository

    @BeforeEach
    fun setUp() {
        val dataSource = EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .build()
        repository = JdbcOnboardingRepository(JdbcTemplate(dataSource))
    }

    @Test
    fun savesAndFindsWithNullableFields() {
        val started = Onboarding(
            id = "ob-1",
            email = "jane@example.com",
            status = OnboardingStatus.STARTED,
            emailCode = "123456",
            createdAt = Instant.now(),
        )

        repository.save(started)

        val found = repository.findById("ob-1").getOrNull()!!
        assertThat(found.email, equalTo("jane@example.com"))
        assertThat(found.status, equalTo(OnboardingStatus.STARTED))
        assertThat(found.emailCode, equalTo("123456"))
        assertThat(found.name, nullValue())
        assertThat(found.creditScore, nullValue())
        assertThat(found.accountId, nullValue())
    }

    @Test
    fun saveUpsertsExistingRow() {
        val started = Onboarding("ob-2", "a@b.com", OnboardingStatus.STARTED, "111111", createdAt = Instant.now())
        repository.save(started)
        repository.save(
            started.copy(
                status = OnboardingStatus.COMPLETED,
                name = "Jane",
                phone = "0812345678",
                sessionToken = "tok",
                creditScore = 720,
                accountId = "AC0000001",
            )
        )

        val found = repository.findById("ob-2").getOrNull()!!
        assertThat(found.status, equalTo(OnboardingStatus.COMPLETED))
        assertThat(found.creditScore, equalTo(720))
        assertThat(found.accountId, equalTo("AC0000001"))
    }

    @Test
    fun returnsNoneForUnknownId() {
        assertThat(repository.findById("nope").isNone(), equalTo(true))
    }

    @Test
    fun deleteAllRemovesEverything() {
        repository.save(Onboarding("ob-3", "a@b.com", OnboardingStatus.STARTED, "222222", createdAt = Instant.now()))
        repository.deleteAll()
        assertThat(repository.findById("ob-3").isNone(), equalTo(true))
    }
}
