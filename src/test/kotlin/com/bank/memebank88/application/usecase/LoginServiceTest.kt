package com.bank.memebank88.onboarding.application.usecase

import com.bank.memebank88.onboarding.adapter.outbound.persistence.InMemoryCustomerRepository
import com.bank.memebank88.onboarding.domain.Customer
import com.bank.memebank88.onboarding.domain.OnboardingError
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class LoginServiceTest {

    private lateinit var customers: InMemoryCustomerRepository
    private lateinit var service: LoginService

    @BeforeEach
    fun setUp() {
        customers = InMemoryCustomerRepository()
        service = LoginService(customers)
        customers.save(
            Customer(
                accountId = "A100",
                email = "jane@example.com",
                name = "Jane Doe",
                phone = "0812345678",
                password = "Ab3Cd",
                creditScore = 720,
                createdAt = Instant.now(),
            ),
        )
    }

    @Test
    fun matchingCredentialsReturnTheCustomer() {
        val result = service.login("jane@example.com", "Ab3Cd")

        assertThat(result.getOrNull()?.accountId, equalTo("A100"))
    }

    @Test
    fun emailIsTrimmedBeforeLookup() {
        val result = service.login("  jane@example.com  ", "Ab3Cd")

        assertThat(result.getOrNull()?.accountId, equalTo("A100"))
    }

    @Test
    fun wrongPasswordIsRejected() {
        val result = service.login("jane@example.com", "wrong")

        assertThat(result.leftOrNull(), instanceOf(OnboardingError.InvalidCredentials::class.java))
    }

    @Test
    fun unknownEmailIsRejected() {
        val result = service.login("nobody@example.com", "Ab3Cd")

        assertThat(result.leftOrNull(), instanceOf(OnboardingError.InvalidCredentials::class.java))
    }
}
