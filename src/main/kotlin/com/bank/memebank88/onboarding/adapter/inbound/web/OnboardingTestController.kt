package com.bank.memebank88.onboarding.adapter.inbound.web

import com.bank.memebank88.onboarding.port.outbound.CustomerRepositoryPort
import com.bank.memebank88.onboarding.port.outbound.OnboardingRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("!prod")
class OnboardingTestController(
    private val onboardingRepository: OnboardingRepositoryPort,
    private val customerRepository: CustomerRepositoryPort,
) {

    @PostMapping("/test/onboarding/reset")
    fun reset() {
        onboardingRepository.deleteAll()
        customerRepository.deleteAll()
    }
}
