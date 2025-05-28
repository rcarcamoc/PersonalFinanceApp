package com.example.personalfinanceapp.data.repository

import android.content.Context
import android.content.Intent
import com.example.personalfinanceapp.domain.repository.AuthRepository
import com.example.personalfinanceapp.domain.repository.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de autenticación.
 * Maneja la lógica de Google Sign-In y la obtención de tokens y permisos.
 */
class AuthRepositoryImpl(private val context: Context) : AuthRepository {

    // Configuración de Google Sign-In con los scopes necesarios para Gmail y Drive
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Solicita el email del usuario
            // Solicita un ID token que puede ser usado para autenticar con un backend (opcional aquí)
            // .requestIdToken("YOUR_SERVER_CLIENT_ID") 
            .requestScopes(
                Scope(GmailScopes.GMAIL_READONLY), // Permiso para leer correos de Gmail
                Scope(DriveScopes.DRIVE_FILE)      // Permiso para acceder a archivos de Drive creados por la app
            )
            .build()
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    override suspend fun signInWithGoogle(): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            // Intenta obtener una cuenta ya autenticada (inicio de sesión silencioso)
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null && !account.isExpired) {
                // Si hay una cuenta válida y no expirada, se considera éxito
                // Aquí se deberían verificar también los scopes concedidos si es necesario
                emit(AuthResult.Success(account.id ?: "Unknown User ID"))
            } else {
                // Si no hay cuenta o está expirada, se necesita un inicio de sesión interactivo.
                // Esto normalmente se maneja devolviendo un Intent para que la UI lo lance.
                // Por ahora, para este ejemplo base, indicamos que se requiere acción del usuario.
                // En una implementación real, se devolvería el Intent o se manejaría el flujo de ActivityResultLauncher.
                emit(AuthResult.Error(Exception("Interactive sign-in required. UI should launch GoogleSignInClient.signInIntent")))
            }
        } catch (e: ApiException) {
            emit(AuthResult.Error(e))
        }
    }

    /**
     * Método para obtener el Intent que la UI debe lanzar para el inicio de sesión interactivo.
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Procesa el resultado del intent de inicio de sesión.
     * Este método sería llamado desde la Activity/Fragment después de que el usuario interactúa con la UI de Google Sign-In.
     */
    suspend fun handleSignInResult(data: Intent?): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            // Inicio de sesión exitoso, puedes obtener el ID del usuario, email, etc.
            // También puedes obtener el token de acceso si lo necesitas para las APIs de Google directamente.
            // val accessToken = account.serverAuthCode // Esto es para el auth code flow, necesitarías el idToken o accessToken
            emit(AuthResult.Success(account.id ?: "Unknown User ID"))
        } catch (e: ApiException) {
            emit(AuthResult.Error(e))
        }
    }


    override suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            // Adicionalmente, si usaste un token para un backend, deberías revocarlo.
            // googleSignInClient.revokeAccess().await() 
        } catch (e: Exception) {
            // Manejar el error, aunque signOut raramente falla si el cliente está inicializado.
            println("Error during sign out: ${e.message}")
        }
    }

    override fun getCurrentUser(): GoogleSignInAccount? {
        // Devuelve la cuenta de Google actualmente autenticada, si existe.
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}

