package com.bank.memebank88.onboarding.port.inbound

import arrow.core.Either
import com.bank.memebank88.onboarding.domain.OnboardingError
import com.bank.memebank88.onboarding.domain.Onboarding

/**
 * Inbound port for the 4-step onboarding flow:
 * verify email → submit info → verify token → credit scoring (creates an account on approval).
 * Every step returns the updated [Onboarding] on the right, or a [OnboardingError] on the left.
 */
interface OnboardingUseCase {
    fun start(email: String): Either<OnboardingError, Onboarding>
    fun verifyEmail(id: String, code: String): Either<OnboardingError, Onboarding>
    fun submitInfo(id: String, name: String, phone: String): Either<OnboardingError, Onboarding>
    fun verifyToken(id: String, token: String): Either<OnboardingError, Onboarding>
    fun score(id: String, salary: Double, occupation: String, monthlyCost: Double, totalWealth: Double): Either<OnboardingError, Onboarding>
    fun find(id: String): Either<OnboardingError, Onboarding>
}
