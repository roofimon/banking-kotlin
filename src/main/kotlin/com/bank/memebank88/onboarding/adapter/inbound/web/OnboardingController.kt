package com.bank.memebank88.onboarding.adapter.inbound.web

import com.bank.memebank88.onboarding.adapter.inbound.web.dto.CustomerInfoRequest
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.OnboardingResponse
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.ScoreRequest
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.StartOnboardingRequest
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.VerifyCodeRequest
import com.bank.memebank88.onboarding.adapter.inbound.web.dto.VerifyTokenRequest
import com.bank.memebank88.onboarding.domain.Onboarding
import com.bank.memebank88.onboarding.port.inbound.OnboardingUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/onboarding")
class OnboardingController(private val onboarding: OnboardingUseCase) {

    @PostMapping
    fun start(@RequestBody request: StartOnboardingRequest): ResponseEntity<*> =
        onboarding.start(request.email).fold(
            { it.toResponse() },
            { ResponseEntity.status(HttpStatus.CREATED).body(it.toDto()) },
        )

    @PostMapping("/{id}/verify-email")
    fun verifyEmail(@PathVariable id: String, @RequestBody request: VerifyCodeRequest): ResponseEntity<*> =
        onboarding.verifyEmail(id, request.code).fold({ it.toResponse() }, { ResponseEntity.ok(it.toDto()) })

    @PostMapping("/{id}/info")
    fun submitInfo(@PathVariable id: String, @RequestBody request: CustomerInfoRequest): ResponseEntity<*> =
        onboarding.submitInfo(id, request.name, request.phoneNumber).fold({ it.toResponse() }, { ResponseEntity.ok(it.toDto()) })

    @PostMapping("/{id}/verify-token")
    fun verifyToken(@PathVariable id: String, @RequestBody request: VerifyTokenRequest): ResponseEntity<*> =
        onboarding.verifyToken(id, request.token).fold({ it.toResponse() }, { ResponseEntity.ok(it.toDto()) })

    @PostMapping("/{id}/score")
    fun score(@PathVariable id: String, @RequestBody request: ScoreRequest): ResponseEntity<*> =
        onboarding.score(id, request.salary, request.occupation, request.monthlyCost, request.totalWealth)
            .fold({ it.toResponse() }, { ResponseEntity.ok(it.toDto()) })

    @GetMapping("/{id}")
    fun find(@PathVariable id: String): ResponseEntity<*> =
        onboarding.find(id).fold({ it.toResponse() }, { ResponseEntity.ok(it.toDto()) })

    private fun Onboarding.toDto() = OnboardingResponse(
        onboardingId = id,
        status = status.name,
        email = email,
        name = name,
        phone = phone,
        emailCode = emailCode,
        sessionToken = sessionToken,
        creditScore = creditScore,
        accountId = accountId,
        password = password,
    )
}
