package com.example.personalfinanceapp.ui.screens.auth

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.personalfinanceapp.domain.repository.AuthResult
import com.example.personalfinanceapp.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

/**
 * Pantalla de inicio de sesión (Login) utilizando Jetpack Compose.
 * Permite al usuario iniciar sesión con Google.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit // Callback para navegar después de un inicio de sesión exitoso
) {
    val signInState by authViewModel.signInState.collectAsState()
    val signInIntent by authViewModel.signInIntent.collectAsState()
    val context = LocalContext.current

    // Launcher para el resultado de la actividad de Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    // authViewModel.handleGoogleSignInResult(intent) // Esta línea necesita que el ViewModel pueda procesar el intent
                    // Procesar el intent directamente aquí o pasarlo al ViewModel si se refactoriza
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                        val account = task.getResult(ApiException::class.java)
                        // Aquí se considera éxito. El ViewModel debería ser notificado.
                        // Por ahora, llamamos directamente a onLoginSuccess para simular el flujo.
                        // En una app real, el ViewModel actualizaría su estado y la UI reaccionaría a eso.
                        if (account != null) {
                             authViewModel.viewModelScope.launch {
                                 authViewModel._signInState.value = AuthResult.Success(account.id ?: "Unknown") 
                             }
                            onLoginSuccess()
                        }
                    } catch (e: ApiException) {
                        authViewModel.viewModelScope.launch {
                           authViewModel._signInState.value = AuthResult.Error(e)
                        }
                    }
                } else {
                     authViewModel.viewModelScope.launch {
                        authViewModel._signInState.value = AuthResult.Error(Exception("Google Sign-In data is null"))
                     }
                }
            } else {
                 authViewModel.viewModelScope.launch {
                    authViewModel._signInState.value = AuthResult.Error(Exception("Google Sign-In failed. Result code: ${result.resultCode}"))
                 }
            }
        }
    )

    // Observar el intent de inicio de sesión y lanzarlo
    LaunchedEffect(signInIntent) {
        signInIntent?.let {
            googleSignInLauncher.launch(it)
            authViewModel.consumeSignInIntent() // Consumir el intent para evitar relanzamientos
        }
    }
    
    // Intento de inicio de sesión silencioso al entrar en la pantalla
    LaunchedEffect(Unit) {
        authViewModel.signInWithGoogle() // Esto intentará un inicio de sesión silencioso
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a PersonalBudget", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        when (val state = signInState) {
            is AuthResult.Loading -> {
                CircularProgressIndicator()
                Text("Iniciando sesión...")
            }
            is AuthResult.Success -> {
                Text("¡Inicio de sesión exitoso!")
                // Navegar a la pantalla principal
                LaunchedEffect(Unit) {
                    onLoginSuccess()
                }
            }
            is AuthResult.Error -> {
                Button(onClick = {
                    // Para el inicio de sesión interactivo, necesitamos el Intent del GoogleSignInClient
                    // Esto idealmente vendría del ViewModel o se construiría aquí si el ViewModel no lo provee.
                    // authViewModel.signInWithGoogle() // Esto podría re-intentar el silencioso o necesitar el interactivo
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(GmailScopes.GMAIL_READONLY), Scope(DriveScopes.DRIVE_FILE))
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }) {
                    Text("Iniciar Sesión con Google")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error: ${state.exception.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

