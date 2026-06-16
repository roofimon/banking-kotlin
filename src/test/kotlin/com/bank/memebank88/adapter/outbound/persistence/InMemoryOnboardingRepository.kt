package com.bank.memebank88.adapter.outbound.persistence

import arrow.core.Option
import arrow.core.toOption
import com.bank.memebank88.domain.Onboarding
import com.bank.memebank88.port.outbound.OnboardingRepositoryPort

/** In-memory [OnboardingRepositoryPort] for use-case tests — load/save across steps. */
class InMemoryOnboardingRepository : OnboardingRepositoryPort {
    private val store = mutableMapOf<String, Onboarding>()

    override fun save(onboarding: Onboarding) {
        store[onboarding.id] = onboarding
    }

    override fun findById(id: String): Option<Onboarding> = store[id].toOption()

    override fun deleteAll() = store.clear()
}
