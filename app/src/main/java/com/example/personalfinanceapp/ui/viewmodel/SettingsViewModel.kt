package com.example.personalfinanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado para la pantalla de configuración de la cuenta de Gmail para ingesta.
 */
data class GmailIngestionAccountState(
    val isLoading: Boolean = false,
    val linkedEmail: String? = null, // Email de la cuenta de Gmail vinculada para ingesta
    val accountStatus: IngestionAccountStatus = IngestionAccountStatus.NOT_LINKED,
    val error: String? = null,
    val showLinkConfirmationDialog: Boolean = false,
    val showUnlinkConfirmationDialog: Boolean = false
)

enum class IngestionAccountStatus {
    NOT_LINKED, // Ninguna cuenta vinculada
    LINKED,     // Cuenta vinculada y operativa
    REVOKED,    // Permisos revocados, necesita re-autorización
    ERROR       // Otro tipo de error
}

/**
 * ViewModel para la pantalla de Ajustes, específicamente para gestionar
 * la cuenta de Gmail utilizada para la ingesta de correos.
 *
 * Este ViewModel interactuará con Casos de Uso para:
 * - Obtener el estado actual de la cuenta de Gmail de ingesta.
 * - Iniciar el flujo de vinculación/cambio de una cuenta de Gmail para ingesta.
 * - Desvincular la cuenta de Gmail de ingesta actual.
 * - Iniciar el flujo de re-autorización si los permisos fueron revocados.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // private val getGmailIngestionAccountStatusUseCase: GetGmailIngestionAccountStatusUseCase, // A ser implementado
    // private val linkGmailIngestionAccountUseCase: LinkGmailIngestionAccountUseCase,       // A ser implementado
    // private val unlinkGmailIngestionAccountUseCase: UnlinkGmailIngestionAccountUseCase     // A ser implementado
) : ViewModel() {

    private val _ingestionAccountState = MutableStateFlow(GmailIngestionAccountState())
    val ingestionAccountState: StateFlow<GmailIngestionAccountState> = _ingestionAccountState.asStateFlow()

    init {
        loadIngestionAccountStatus()
    }

    fun loadIngestionAccountStatus() {
        viewModelScope.launch {
            _ingestionAccountState.value = _ingestionAccountState.value.copy(isLoading = true)
            // Simulación: En una implementación real, se llamaría a un UseCase
            // que obtiene los datos de SharedPreferences o similar.
            // val result = getGmailIngestionAccountStatusUseCase()
            // result.fold(
            //     onSuccess = { status -> _ingestionAccountState.value = status },
            //     onFailure = { e -> _ingestionAccountState.value = GmailIngestionAccountState(error = e.message) }
            // )
            // Placeholder data:
            _ingestionAccountState.value = GmailIngestionAccountState(
                isLoading = false,
                // linkedEmail = "test.ingestion@gmail.com",
                // accountStatus = IngestionAccountStatus.LINKED
                linkedEmail = null,
                accountStatus = IngestionAccountStatus.NOT_LINKED
            )
        }
    }

    fun onLinkOrChangeAccountClicked() {
        if (_ingestionAccountState.value.linkedEmail != null) {
            // Si ya hay una cuenta, mostrar diálogo de confirmación para cambiar
            _ingestionAccountState.value = _ingestionAccountState.value.copy(showLinkConfirmationDialog = true)
        } else {
            // Si no hay cuenta, iniciar flujo de vinculación directamente
            initiateLinkAccountFlow()
        }
    }

    fun confirmLinkOrChangeAccount() {
        _ingestionAccountState.value = _ingestionAccountState.value.copy(showLinkConfirmationDialog = false)
        initiateLinkAccountFlow()
    }

    fun cancelLinkOrChangeAccount() {
        _ingestionAccountState.value = _ingestionAccountState.value.copy(showLinkConfirmationDialog = false)
    }

    private fun initiateLinkAccountFlow() {
        viewModelScope.launch {
            _ingestionAccountState.value = _ingestionAccountState.value.copy(isLoading = true)
            // Aquí se llamaría a linkGmailIngestionAccountUseCase, que a su vez
            // interactuaría con SecondaryAuthManager para lanzar el intent de OAuth.
            // El resultado de ese intent (éxito/fracaso, nuevo email) actualizaría el estado.
            // Simulación de éxito después de un delay:
            // kotlinx.coroutines.delay(2000)
            // _ingestionAccountState.value = GmailIngestionAccountState(
            //     isLoading = false,
            //     linkedEmail = "new.ingestion@gmail.com",
            //     accountStatus = IngestionAccountStatus.LINKED
            // )
            // Por ahora, solo indicamos que se inició (la lógica real es más compleja y depende de ActivityResultLauncher)
            _ingestionAccountState.value = _ingestionAccountState.value.copy(isLoading = false, error = "Flujo de vinculación iniciado (simulado). Implementación pendiente.")
        }
    }

    fun onUnlinkAccountClicked() {
        _ingestionAccountState.value = _ingestionAccountState.value.copy(showUnlinkConfirmationDialog = true)
    }

    fun confirmUnlinkAccount() {
        viewModelScope.launch {
            _ingestionAccountState.value = _ingestionAccountState.value.copy(isLoading = true, showUnlinkConfirmationDialog = false)
            // Aquí se llamaría a unlinkGmailIngestionAccountUseCase.
            // Simulación de éxito:
            // kotlinx.coroutines.delay(1000)
            // _ingestionAccountState.value = GmailIngestionAccountState(
            //     isLoading = false,
            //     linkedEmail = null,
            //     accountStatus = IngestionAccountStatus.NOT_LINKED
            // )
            _ingestionAccountState.value = _ingestionAccountState.value.copy(isLoading = false, linkedEmail = null, accountStatus = IngestionAccountStatus.NOT_LINKED, error = "Cuenta desvinculada (simulado).")
        }
    }

    fun cancelUnlinkAccount() {
        _ingestionAccountState.value = _ingestionAccountState.value.copy(showUnlinkConfirmationDialog = false)
    }

    fun onReauthorizeClicked() {
        viewModelScope.launch {
            // Similar a initiateLinkAccountFlow, pero podría pasar el email actual para re-autorizar.
            _ingestionAccountState.value = _ingestionAccountState.value.copy(isLoading = false, error = "Flujo de re-autorización iniciado (simulado). Implementación pendiente.")
        }
    }

    fun consumeError() {
        _ingestionAccountState.value = _ingestionAccountState.value.copy(error = null)
    }
}

