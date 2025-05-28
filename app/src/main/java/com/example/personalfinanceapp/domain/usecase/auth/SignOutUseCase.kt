package com.example.personalfinanceapp.domain.usecase.auth

import com.example.personalfinanceapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar la sesión del usuario.
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
    }
}

