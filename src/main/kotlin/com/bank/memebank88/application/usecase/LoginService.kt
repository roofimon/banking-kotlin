package com.bank.memebank88.application.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.bank.memebank88.domain.Customer
import com.bank.memebank88.domain.DomainError
import com.bank.memebank88.port.inbound.LoginUseCase
import com.bank.memebank88.port.outbound.CustomerRepositoryPort
import org.springframework.stereotype.Service

@Service
class LoginService(private val customers: CustomerRepositoryPort) : LoginUseCase {

    override fun login(email: String, password: String): Either<DomainError, Customer> = either {
        val customer = customers.findByEmail(email.trim())
            .toEither { DomainError.InvalidCredentials }
            .bind()
        // Demo-grade check: passwords are stored in plaintext (see OnboardingService.randomPassword).
        ensure(customer.password == password) { DomainError.InvalidCredentials }
        customer
    }
}
