package com.bank.memebank88.onboarding.application.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.bank.memebank88.onboarding.domain.Customer
import com.bank.memebank88.onboarding.domain.OnboardingError
import com.bank.memebank88.onboarding.port.inbound.LoginUseCase
import com.bank.memebank88.onboarding.port.outbound.CustomerRepositoryPort
import org.springframework.stereotype.Service

@Service
class LoginService(private val customers: CustomerRepositoryPort) : LoginUseCase {

    override fun login(email: String, password: String): Either<OnboardingError, Customer> = either {
        val customer = customers.findByEmail(email.trim())
            .toEither { OnboardingError.InvalidCredentials }
            .bind()
        // Demo-grade check: passwords are stored in plaintext (see OnboardingService.randomPassword).
        ensure(customer.password == password) { OnboardingError.InvalidCredentials }
        customer
    }
}
