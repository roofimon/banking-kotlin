package com.bank.memebank88.onboarding.domain.event

import java.time.Instant

/**
 * Domain events emitted as an onboarding session moves through its lifecycle
 * (`STARTED → EMAIL_VERIFIED → INFO_SUBMITTED → TOKEN_VERIFIED → COMPLETED | REJECTED`).
 *
 * Published on the in-process Spring application event bus by the onboarding application service
 * after each transition is persisted. There is no consumer yet — these are a seam other features
 * (welcome email, analytics, …) can subscribe to with `@EventListener`.
 */
sealed interface OnboardingEvent {
    val onboardingId: String
    val occurredAt: Instant

    data class OnboardingStarted(
        override val onboardingId: String,
        val email: String,
        override val occurredAt: Instant,
    ) : OnboardingEvent

    data class OnboardingEmailVerified(
        override val onboardingId: String,
        val email: String,
        override val occurredAt: Instant,
    ) : OnboardingEvent

    data class OnboardingInfoSubmitted(
        override val onboardingId: String,
        val name: String,
        val phone: String,
        override val occurredAt: Instant,
    ) : OnboardingEvent

    data class OnboardingTokenVerified(
        override val onboardingId: String,
        override val occurredAt: Instant,
    ) : OnboardingEvent

    data class OnboardingApproved(
        override val onboardingId: String,
        val email: String,
        val accountId: String,
        val creditScore: Int,
        override val occurredAt: Instant,
    ) : OnboardingEvent

    data class OnboardingRejected(
        override val onboardingId: String,
        val email: String,
        val creditScore: Int,
        override val occurredAt: Instant,
    ) : OnboardingEvent
}
