package com.bank.memebank88.onboarding.domain

/**
 * The onboarding module's failure vocabulary (onboarding flow + login), modelled as data rather
 * than thrown exceptions. Operations that can fail return `Either<OnboardingError, T>`.
 */
sealed interface OnboardingError {

    /** No onboarding session exists for [onboardingId]. */
    data class OnboardingNotFound(val onboardingId: String) : OnboardingError

    /** An onboarding step was attempted in the wrong order. */
    data class OnboardingStepOutOfOrder(val expected: String, val actual: String) : OnboardingError

    /** A verification code/token did not match. [kind] is "email" or "token". */
    data class VerificationFailed(val kind: String) : OnboardingError

    /** Customer info (name/phone) failed validation. */
    data class InvalidCustomerInfo(val reason: String) : OnboardingError

    /** Login email/password did not match any customer. */
    data object InvalidCredentials : OnboardingError
}
