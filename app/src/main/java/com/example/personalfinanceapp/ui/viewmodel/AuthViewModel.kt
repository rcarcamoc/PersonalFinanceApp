package com.example.personalfinanceapp.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.domain.repository.AuthResult
import com.example.personalfinanceapp.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.personalfinanceapp.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel // Asumiendo que usarás Hilt para DI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejar la lógica de autenticación.
 *
 * @property signInWithGoogleUseCase Caso de uso para iniciar sesión con Google.
 * @property signOutUseCase Caso de uso para cerrar sesión.
 */
@HiltViewModel // Anotación para Hilt, necesitarás configurar Hilt en el proyecto
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase
    // private val authRepositoryImpl: AuthRepositoryImpl // Para obtener el Intent, esto podría necesitar un refactor o una forma diferente de obtener el intent
) : ViewModel() {

    private val _signInState = MutableStateFlow<AuthResult>(AuthResult.Error(Exception("Not signed in")))
    val signInState: StateFlow<AuthResult> = _signInState.asStateFlow()

    // Este StateFlow podría usarse para solicitar a la UI que lance el Intent de Google Sign-In
    private val _signInIntent = MutableStateFlow<Intent?>(null)
    val signInIntent: StateFlow<Intent?> = _signInIntent.asStateFlow()

    /**
     * Inicia el proceso de inicio de sesión con Google.
     * En una implementación real, esto podría necesitar obtener un Intent del AuthRepositoryImpl
     * y emitirlo a través de _signInIntent para que la Activity/Composable lo lance.
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            // Aquí es donde la lógica para obtener el Intent de signIn se complica un poco con Clean Architecture.
            // Una opción es que el AuthRepository devuelva el Intent o una señal para pedirlo.
            // Por ahora, simularemos que el UseCase maneja esto internamente o que la UI lo obtiene directamente.
            // _signInIntent.value = authRepositoryImpl.getSignInIntent() // Esto requeriría inyectar AuthRepositoryImpl o un provider
            
            // Este es el flujo si el inicio de sesión silencioso o una acción directa es posible desde el ViewModel
            signInWithGoogleUseCase().collect {
                _signInState.value = it
            }
        }
    }

    /**
     * Maneja el resultado del Intent de inicio de sesión de Google.
     * Este método sería llamado desde la UI después de que el ActivityResultLauncher complete.
     */
    fun handleGoogleSignInResult(intent: Intent?) {
        viewModelScope.launch {
            // Aquí necesitarías un caso de uso o método en el repositorio para procesar el resultado del intent
            // Por ejemplo: authRepository.handleSignInResult(intent).collect { _signInState.value = it }
            // Esto es un placeholder, ya que AuthRepositoryImpl tiene handleSignInResult, pero no está en la interfaz AuthRepository
            // Se necesitaría un refactor para exponer esta funcionalidad a través de la capa de dominio correctamente.
            if (intent != null) {
                // Simulación de procesamiento del resultado
                // En una implementación real, llamarías a un método en tu AuthRepository/UseCase
                // que tome el 'intent' y devuelva un AuthResult.
                // Por ejemplo, si AuthRepositoryImpl.handleSignInResult fuera accesible a través de un UseCase:
                // handleSignInResultUseCase(intent).collect { result -> _signInState.value = result }
                _signInState.value = AuthResult.Loading // Indicar carga mientras se procesa
                // Aquí iría la lógica real para procesar el intent y actualizar _signInState
            } else {
                _signInState.value = AuthResult.Error(Exception("Sign-in intent was null"))
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _signInState.value = AuthResult.Error(Exception("Signed out")) // Actualizar estado a no autenticado
        }
    }

    /**
     * Resetea el estado del Intent de inicio de sesión para evitar que se lance múltiples veces.
     */
    fun consumeSignInIntent() {
        _signInIntent.value = null
    }
}

