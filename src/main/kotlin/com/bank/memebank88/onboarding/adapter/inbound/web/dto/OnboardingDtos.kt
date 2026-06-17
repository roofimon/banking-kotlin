package com.bank.memebank88.onboarding.adapter.inbound.web.dto

// --- request bodies ---
data class StartOnboardingRequest(val email: String)
data class VerifyCodeRequest(val code: String)
data class CustomerInfoRequest(val name: String, val phoneNumber: String)
data class VerifyTokenRequest(val token: String)
data class ScoreRequest(val salary: Double, val occupation: String, val monthlyCost: Double, val totalWealth: Double)

/**
 * Onboarding state returned to the caller. [emailCode] and [sessionToken] are mock dev/test
 * conveniences (in a real system they would be delivered out-of-band, never echoed back).
 */
data class OnboardingResponse(
    val onboardingId: String,
    val status: String,
    val email: String,
    val name: String?,
    val phone: String?,
    val emailCode: String?,
    val sessionToken: String?,
    val creditScore: Int?,
    val accountId: String?,
    val password: String?,
)
