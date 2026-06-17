package com.bank.memebank88.onboarding.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import java.time.Instant

enum class OnboardingStatus { STARTED, EMAIL_VERIFIED, INFO_SUBMITTED, TOKEN_VERIFIED, COMPLETED, REJECTED }

/**
 * Onboarding aggregate — a 4-step state machine modelled as immutable data. Transitions are pure and
 * return [Either]; they enforce step order and code/token matching. Side effects (code generation,
 * scoring, account creation) live in the use case, which passes the resulting values in here.
 */
data class Onboarding(
    val id: String,
    val email: String,
    val status: OnboardingStatus,
    val emailCode: String,
    val name: String? = null,
    val phone: String? = null,
    val sessionToken: String? = null,
    val creditScore: Int? = null,
    val accountId: String? = null,
    /**
     * Generated login password, populated only on the approval response. Its persistent home is
     * the CUSTOMER table, so it is intentionally not stored in ONBOARDING.
     */
    val password: String? = null,
    val createdAt: Instant,
) {

    /** Step 1: confirm the email with the issued [code]. */
    fun verifyEmail(code: String): Either<OnboardingError, Onboarding> = either {
        ensure(status == OnboardingStatus.STARTED) { stepError(OnboardingStatus.STARTED) }
        ensure(code == emailCode) { OnboardingError.VerificationFailed("email") }
        copy(status = OnboardingStatus.EMAIL_VERIFIED)
    }

    /** Step 2: capture customer info; [token] is the freshly issued session token. */
    fun submitInfo(name: String, phone: String, token: String): Either<OnboardingError, Onboarding> = either {
        ensure(status == OnboardingStatus.EMAIL_VERIFIED) { stepError(OnboardingStatus.EMAIL_VERIFIED) }
        ensure(name.isNotBlank()) { OnboardingError.InvalidCustomerInfo("name must not be blank") }
        ensure(phone.isNotBlank()) { OnboardingError.InvalidCustomerInfo("phone must not be blank") }
        copy(status = OnboardingStatus.INFO_SUBMITTED, name = name, phone = phone, sessionToken = token)
    }

    /** Step 3: confirm the server-issued session [token]. */
    fun verifyToken(token: String): Either<OnboardingError, Onboarding> = either {
        ensure(status == OnboardingStatus.INFO_SUBMITTED) { stepError(OnboardingStatus.INFO_SUBMITTED) }
        ensure(token == sessionToken) { OnboardingError.VerificationFailed("token") }
        copy(status = OnboardingStatus.TOKEN_VERIFIED)
    }

    /** Step 4: record the credit decision; [accountId] is set only when [approved]. */
    fun complete(score: Int, approved: Boolean, accountId: String?): Either<OnboardingError, Onboarding> = either {
        ensure(status == OnboardingStatus.TOKEN_VERIFIED) { stepError(OnboardingStatus.TOKEN_VERIFIED) }
        copy(
            status = if (approved) OnboardingStatus.COMPLETED else OnboardingStatus.REJECTED,
            creditScore = score,
            accountId = if (approved) accountId else null,
        )
    }

    private fun stepError(expected: OnboardingStatus) =
        OnboardingError.OnboardingStepOutOfOrder(expected.name, status.name)
}
