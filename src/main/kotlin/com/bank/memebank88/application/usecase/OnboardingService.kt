package com.bank.memebank88.application.usecase

import arrow.core.Either
import arrow.core.raise.either
import com.bank.memebank88.domain.Customer
import com.bank.memebank88.domain.DomainError
import com.bank.memebank88.domain.Onboarding
import com.bank.memebank88.domain.OnboardingStatus
import com.bank.memebank88.port.inbound.OnboardingUseCase
import com.bank.memebank88.port.outbound.AccountProvisioningPort
import com.bank.memebank88.port.outbound.CreditScoringPort
import com.bank.memebank88.port.outbound.CustomerRepositoryPort
import com.bank.memebank88.port.outbound.OnboardingRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

@Service
class OnboardingService(
    private val repository: OnboardingRepositoryPort,
    private val creditScoring: CreditScoringPort,
    private val accountProvisioning: AccountProvisioningPort,
    private val customers: CustomerRepositoryPort,
) : OnboardingUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun start(email: String): Either<DomainError, Onboarding> = either {
        val emailCode = randomCode()
        log.info("Onboarding email verification code for {}: {}", email, emailCode)
        val onboarding = Onboarding(
            id = UUID.randomUUID().toString(),
            email = email,
            status = OnboardingStatus.STARTED,
            emailCode = emailCode,
            createdAt = Instant.now(),
        )
        repository.save(onboarding)
        onboarding
    }

    override fun verifyEmail(id: String, code: String): Either<DomainError, Onboarding> = either {
        val updated = load(id).bind().verifyEmail(code).bind()
        repository.save(updated)
        updated
    }

    override fun submitInfo(id: String, name: String, phone: String): Either<DomainError, Onboarding> = either {
        val token = UUID.randomUUID().toString()
        log.info("Onboarding session token for {}: {}", id, token)
        val updated = load(id).bind().submitInfo(name, phone, token).bind()
        repository.save(updated)
        updated
    }

    override fun verifyToken(id: String, token: String): Either<DomainError, Onboarding> = either {
        val updated = load(id).bind().verifyToken(token).bind()
        repository.save(updated)
        updated
    }

    override fun score(id: String, salary: Double, occupation: String, monthlyCost: Double, totalWealth: Double): Either<DomainError, Onboarding> = either {
        val onboarding = load(id).bind()
        val decision = creditScoring.assess(salary, occupation, monthlyCost, totalWealth)
        val accountId = if (decision.approved) accountProvisioning.createAccount(0.0) else null
        val completed = onboarding.complete(decision.score, decision.approved, accountId).bind()
            .let { if (it.status == OnboardingStatus.COMPLETED) it.copy(password = randomPassword()) else it }
        repository.save(completed)
        if (completed.status == OnboardingStatus.COMPLETED && completed.accountId != null) {
            customers.save(completed.toCustomer())
            log.info("Onboarding {} approved; saved customer for account {}", id, completed.accountId)
        }
        completed
    }

    /** Snapshot the approved applicant as a persistent customer record. */
    private fun Onboarding.toCustomer() = Customer(
        accountId = requireNotNull(accountId),
        email = email,
        name = name.orEmpty(),
        phone = phone.orEmpty(),
        password = requireNotNull(password),
        creditScore = creditScore ?: 0,
        createdAt = Instant.now(),
    )

    override fun find(id: String): Either<DomainError, Onboarding> = either {
        load(id).bind()
    }

    private fun load(id: String) =
        repository.findById(id).toEither { DomainError.OnboardingNotFound(id) }

    private fun randomCode(): String = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')

    private fun randomPassword(): String = (1..PASSWORD_LENGTH).map { PASSWORD_CHARS.random() }.joinToString("")

    private companion object {
        private const val PASSWORD_LENGTH = 5
        private const val PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}
