package com.bank.memebank88.port.outbound

import arrow.core.Option
import com.bank.memebank88.domain.Onboarding

interface OnboardingRepositoryPort {
    fun save(onboarding: Onboarding)
    fun findById(id: String): Option<Onboarding>
    fun deleteAll()
}
