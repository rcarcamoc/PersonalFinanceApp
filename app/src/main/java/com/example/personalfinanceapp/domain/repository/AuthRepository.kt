package com.example.personalfinanceapp.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de autenticación.
 * Define los métodos para iniciar sesión y cerrar sesión.
 */
interface AuthRepository {
    /**
     * Inicia el proceso de inicio de sesión con Google.
     * Devuelve un Flow que emite el estado de la autenticación (ej. éxito, error, usuario).
     */
    suspend fun signInWithGoogle(): Flow<AuthResult>

    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun signOut()

    /**
     * Verifica si hay un usuario actualmente autenticado.
     */
    fun getCurrentUser(): Any? // Puede ser un modelo de Usuario específico
}

/**
 * Representa el resultado de una operación de autenticación.
 */
sealed class AuthResult {
    data class Success(val userId: String) : AuthResult() // Podría incluir más datos del usuario
    data class Error(val exception: Exception) : AuthResult()
    object Loading : AuthResult()
}

