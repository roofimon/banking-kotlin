package com.bank.memebank88.adapter.inbound.web.dto

data class LoginRequest(val email: String, val password: String)

/** Customer identity returned on a successful login. */
data class LoginResponse(
    val accountId: String,
    val email: String,
    val name: String,
    val creditScore: Int,
)
