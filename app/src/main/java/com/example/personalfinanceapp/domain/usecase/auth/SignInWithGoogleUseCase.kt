package com.example.personalfinanceapp.domain.usecase.auth

import com.example.personalfinanceapp.domain.repository.AuthRepository
import com.example.personalfinanceapp.domain.repository.AuthResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión con Google.
 * Encapsula la lógica de negocio para el inicio de sesión.
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<AuthResult> {
        return authRepository.signInWithGoogle()
    }
}

