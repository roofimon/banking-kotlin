package com.bank.memebank88

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

/**
 * Verifies the modular-monolith boundaries: the application splits into the `banking`,
 * `onboarding`, and `shared` modules, and `verify()` fails the build on any illegal cross-module
 * access or dependency cycle. Onboarding must not depend on banking code (it shares only the
 * database); both may depend on `shared`.
 */
class ModularityTests {

    private val modules = ApplicationModules.of(MemeBank88Application::class.java)

    @Test
    fun verifiesModuleStructure() {
        modules.verify()
    }
}
