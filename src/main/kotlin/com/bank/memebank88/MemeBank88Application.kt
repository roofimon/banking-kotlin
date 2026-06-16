package com.bank.memebank88

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class MemeBank88Application

fun main(args: Array<String>) {
	runApplication<MemeBank88Application>(*args)
}
