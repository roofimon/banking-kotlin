package com.bank.memebank88.adapter.inbound.web

import com.bank.memebank88.adapter.inbound.web.dto.LoginRequest
import com.bank.memebank88.adapter.inbound.web.dto.LoginResponse
import com.bank.memebank88.domain.Customer
import com.bank.memebank88.port.inbound.LoginUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class LoginController(private val login: LoginUseCase) {

    @PostMapping
    fun login(@RequestBody request: LoginRequest): ResponseEntity<*> =
        login.login(request.email, request.password).fold(
            { it.toResponse() },
            { ResponseEntity.ok(it.toDto()) },
        )

    private fun Customer.toDto() = LoginResponse(
        accountId = accountId,
        email = email,
        name = name,
        creditScore = creditScore,
    )
}
