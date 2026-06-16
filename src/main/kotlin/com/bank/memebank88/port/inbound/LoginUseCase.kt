package com.bank.memebank88.port.inbound

import arrow.core.Either
import com.bank.memebank88.domain.Customer
import com.bank.memebank88.domain.DomainError

/**
 * Inbound port for customer login. Authenticates an email/password pair against the customer
 * records created during onboarding, returning the matching [Customer] or
 * [DomainError.InvalidCredentials].
 */
interface LoginUseCase {
    fun login(email: String, password: String): Either<DomainError, Customer>
}
