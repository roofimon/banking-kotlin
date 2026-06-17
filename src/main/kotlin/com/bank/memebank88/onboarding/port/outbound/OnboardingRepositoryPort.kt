package com.bank.memebank88.onboarding.port.outbound

import arrow.core.Option
import com.bank.memebank88.onboarding.domain.Onboarding

interface OnboardingRepositoryPort {
    fun save(onboarding: Onboarding)
    fun findById(id: String): Option<Onboarding>
    fun deleteAll()
}
