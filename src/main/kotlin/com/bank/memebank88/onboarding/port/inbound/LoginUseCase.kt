package com.bank.memebank88.onboarding.port.inbound

import arrow.core.Either
import com.bank.memebank88.onboarding.domain.Customer
import com.bank.memebank88.onboarding.domain.OnboardingError

/**
 * Inbound port for customer login. Authenticates an email/password pair against the customer
 * records created during onboarding, returning the matching [Customer] or
 * [OnboardingError.InvalidCredentials].
 */
interface LoginUseCase {
    fun login(email: String, password: String): Either<OnboardingError, Customer>
}
