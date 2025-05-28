package com.example.personalfinanceapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.personalfinanceapp.domain.repository.AuthRepository
import com.example.personalfinanceapp.domain.repository.AuthResult
import com.example.personalfinanceapp.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.personalfinanceapp.domain.usecase.auth.SignOutUseCase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount // Necesitarás mockear esto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.* // Importar runTest, TestCoroutineScheduler, etc.
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    // private lateinit var authRepository: AuthRepository // Si se necesita para el Intent
    private lateinit var authViewModel: AuthViewModel

    private val mockGoogleSignInAccount: GoogleSignInAccount = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mock()
        signOutUseCase = mock()
        // authRepository = mock() // Si se usa para el intent

        // Configurar el mock de GoogleSignInAccount
        whenever(mockGoogleSignInAccount.id).thenReturn("testUserId")
        whenever(mockGoogleSignInAccount.email).thenReturn("test@example.com")

        authViewModel = AuthViewModel(signInWithGoogleUseCase, signOutUseCase /*, authRepository */)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle SHOULD update state to Loading then Success on successful sign-in`() = runTest {
        // Arrange
        val successResult = AuthResult.Success("testUserId")
        whenever(signInWithGoogleUseCase.invoke()).thenReturn(flowOf(AuthResult.Loading, successResult))

        // Act
        authViewModel.signInWithGoogle()
        advanceUntilIdle() // Asegurar que todas las corrutinas del ViewModel se completen

        // Assert
        assertEquals(successResult, authViewModel.signInState.value)
        verify(signInWithGoogleUseCase).invoke()
    }

    @Test
    fun `signInWithGoogle SHOULD update state to Loading then Error on failed sign-in`() = runTest {
        // Arrange
        val exception = Exception("Sign-in failed")
        val errorResult = AuthResult.Error(exception)
        whenever(signInWithGoogleUseCase.invoke()).thenReturn(flowOf(AuthResult.Loading, errorResult))

        // Act
        authViewModel.signInWithGoogle()
        advanceUntilIdle()

        // Assert
        assertEquals(errorResult, authViewModel.signInState.value)
        verify(signInWithGoogleUseCase).invoke()
    }

    @Test
    fun `signOut SHOULD call signOutUseCase and update state to Error`() = runTest {
        // Arrange
        // No se necesita mockear signOutUseCase.invoke() si no devuelve nada específico que se observe

        // Act
        authViewModel.signOut()
        advanceUntilIdle()

        // Assert
        verify(signOutUseCase).invoke()
        assertTrue("signInState should be Error after sign out", authViewModel.signInState.value is AuthResult.Error)
        assertEquals("Signed out", (authViewModel.signInState.value as AuthResult.Error).exception.message)
    }

    // Las pruebas para handleGoogleSignInResult y signInIntent son más complejas
    // porque involucran Intents y la interacción con el ActivityResultLauncher.
    // Estas a menudo se prueban mejor con pruebas de instrumentación o refactorizando
    // la lógica para que el ViewModel no maneje directamente el Intent.

    // Ejemplo de prueba para consumeSignInIntent
    @Test
    fun `consumeSignInIntent SHOULD set signInIntent to null`() = runTest {
        // Arrange
        // Simular que un intent fue emitido (esto normalmente vendría de una lógica más compleja)
        // authViewModel._signInIntent.value = mock() // Necesitarías un mock de Intent

        // Act
        authViewModel.consumeSignInIntent()
        advanceUntilIdle()

        // Assert
        assertNull("signInIntent should be null after consumption", authViewModel.signInIntent.value)
    }
}

