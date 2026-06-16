package com.bank.memebank88.port.inbound

import arrow.core.Either
import com.bank.memebank88.domain.DomainError
import com.bank.memebank88.domain.Onboarding

/**
 * Inbound port for the 4-step onboarding flow:
 * verify email → submit info → verify token → credit scoring (creates an account on approval).
 * Every step returns the updated [Onboarding] on the right, or a [DomainError] on the left.
 */
interface OnboardingUseCase {
    fun start(email: String): Either<DomainError, Onboarding>
    fun verifyEmail(id: String, code: String): Either<DomainError, Onboarding>
    fun submitInfo(id: String, name: String, phone: String): Either<DomainError, Onboarding>
    fun verifyToken(id: String, token: String): Either<DomainError, Onboarding>
    fun score(id: String, salary: Double, occupation: String, monthlyCost: Double, totalWealth: Double): Either<DomainError, Onboarding>
    fun find(id: String): Either<DomainError, Onboarding>
}
