package com.example.personalfinanceapp.ui.viewmodel.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.local.sharing.SharedUserEntity
import com.example.personalfinanceapp.data.local.sharing.SharingInvitationEntity
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import com.example.personalfinanceapp.domain.usecase.sharing.SharingUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado para la pantalla de compartición.
 */
data class SharingScreenState(
    val isLoading: Boolean = false,
    val receivedInvitations: List<SharingInvitationEntity> = emptyList(),
    val sentInvitations: List<SharingInvitationEntity> = emptyList(),
    val sharedUsers: List<SharedUserEntity> = emptyList(), // Usuarios con los que yo comparto o me comparten
    val myDriveFileId: String? = null, // ID de mi archivo de datos en Drive para compartir
    val snackbarMessage: String? = null,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de Compartición.
 * Maneja la lógica para invitar usuarios, aceptar/rechazar invitaciones, gestionar usuarios compartidos
 * y sincronizar datos.
 */
@HiltViewModel
class SharingViewModel @Inject constructor(
    private val sharingUseCases: SharingUseCases
) : ViewModel() {

    private val _sharingState = MutableStateFlow(SharingScreenState())
    val sharingState: StateFlow<SharingScreenState> = _sharingState.asStateFlow()

    // Placeholder para el email del usuario actual. En una app real, esto vendría del AuthViewModel o similar.
    private val currentUserEmail = "me@example.com"

    init {
        loadInitialData()
        ensureMyDataIsSharable()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _sharingState.value = _sharingState.value.copy(isLoading = true)
            try {
                sharingUseCases.getReceivedInvitations(currentUserEmail)
                    .onEach { invitations ->
                        _sharingState.value = _sharingState.value.copy(receivedInvitations = invitations)
                    }.launchIn(viewModelScope)

                sharingUseCases.getSentInvitations(currentUserEmail)
                    .onEach { invitations ->
                        _sharingState.value = _sharingState.value.copy(sentInvitations = invitations)
                    }.launchIn(viewModelScope)

                sharingUseCases.getSharedUsers()
                    .onEach { users ->
                        _sharingState.value = _sharingState.value.copy(sharedUsers = users, isLoading = false)
                    }.catch { e ->
                        _sharingState.value = _sharingState.value.copy(isLoading = false, error = e.message)
                    }.launchIn(viewModelScope)

            } catch (e: Exception) {
                _sharingState.value = _sharingState.value.copy(isLoading = false, error = e.message ?: "Error al cargar datos de compartición")
            }
        }
    }

    private fun ensureMyDataIsSharable() {
        viewModelScope.launch {
            sharingUseCases.ensureMyDataIsSharable()
                .onEach { result ->
                    result.fold(
                        onSuccess = { fileId ->
                            _sharingState.value = _sharingState.value.copy(myDriveFileId = fileId)
                            if (fileId == null) {
                                _sharingState.value = _sharingState.value.copy(snackbarMessage = "No se pudo preparar tu archivo para compartir.")
                            }
                        },
                        onFailure = { e ->
                            _sharingState.value = _sharingState.value.copy(snackbarMessage = "Error al preparar datos para compartir: ${e.message}")
                        }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(snackbarMessage = "Excepción al preparar datos: ${e.message}") }
                .launchIn(viewModelScope)
        }
    }

    fun sendInvitation(email: String, role: UserRole) {
        val myFileId = _sharingState.value.myDriveFileId
        if (myFileId == null) {
            _sharingState.value = _sharingState.value.copy(snackbarMessage = "Tu archivo de datos aún no está listo para compartir. Intenta de nuevo en un momento.")
            ensureMyDataIsSharable() // Reintentar
            return
        }
        viewModelScope.launch {
            sharingUseCases.sendSharingInvitation(email, role, myFileId)
                .onEach { result ->
                    result.fold(
                        onSuccess = { invitation ->
                            _sharingState.value = _sharingState.value.copy(snackbarMessage = "Invitación enviada a ${invitation.invitedUserEmail}")
                            // La lista de invitaciones enviadas se actualizará por el Flow
                        },
                        onFailure = { e ->
                            _sharingState.value = _sharingState.value.copy(error = e.message ?: "Error al enviar invitación")
                        }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Excepción al enviar invitación") }
                .launchIn(viewModelScope)
        }
    }

    fun acceptInvitation(invitationId: String) {
        viewModelScope.launch {
            sharingUseCases.acceptSharingInvitation(invitationId)
                .onEach { result ->
                    result.fold(
                        onSuccess = { sharedUser ->
                            _sharingState.value = _sharingState.value.copy(snackbarMessage = "Invitación aceptada de ${sharedUser.email}")
                            // Las listas se actualizarán por los Flows
                        },
                        onFailure = { e ->
                            _sharingState.value = _sharingState.value.copy(error = e.message ?: "Error al aceptar invitación")
                        }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Excepción al aceptar invitación") }
                .launchIn(viewModelScope)
        }
    }

    fun rejectInvitation(invitationId: String) {
        viewModelScope.launch {
            sharingUseCases.rejectSharingInvitation(invitationId)
                .onEach { result ->
                    result.fold(
                        onSuccess = {
                            _sharingState.value = _sharingState.value.copy(snackbarMessage = "Invitación rechazada.")
                            // La lista se actualizará
                        },
                        onFailure = { e ->
                            _sharingState.value = _sharingState.value.copy(error = e.message ?: "Error al rechazar invitación")
                        }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Excepción al rechazar invitación") }
                .launchIn(viewModelScope)
        }
    }

    fun syncWithUser(sharedUser: SharedUserEntity) {
        if (sharedUser.theirDriveFileId == null) {
            _sharingState.value = _sharingState.value.copy(snackbarMessage = "Este usuario no tiene un archivo de Drive configurado para sincronizar.")
            return
        }
        viewModelScope.launch {
            _sharingState.value = _sharingState.value.copy(isLoading = true, snackbarMessage = "Sincronizando con ${sharedUser.email}...")
            sharingUseCases.syncDataFromSharedUser(sharedUser)
                .onEach { result ->
                    result.fold(
                        onSuccess = {
                            _sharingState.value = _sharingState.value.copy(isLoading = false, snackbarMessage = "Sincronización con ${sharedUser.email} completada.")
                        },
                        onFailure = { e ->
                            _sharingState.value = _sharingState.value.copy(isLoading = false, error = "Error al sincronizar con ${sharedUser.email}: ${e.message}")
                        }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(isLoading = false, error = "Excepción al sincronizar: ${e.message}") }
                .launchIn(viewModelScope)
        }
    }

    fun removeSharedUser(userId: String) {
        viewModelScope.launch {
            sharingUseCases.removeSharedUser(userId)
                .onEach { result ->
                    result.fold(
                        onSuccess = { _sharingState.value = _sharingState.value.copy(snackbarMessage = "Usuario eliminado de la lista de compartidos.") },
                        onFailure = { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Error al eliminar usuario.") }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Excepción al eliminar usuario.") }
                .launchIn(viewModelScope)
        }
    }
    
    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            sharingUseCases.updateSharedUserRole(userId, newRole)
                .onEach { result ->
                    result.fold(
                        onSuccess = { _sharingState.value = _sharingState.value.copy(snackbarMessage = "Rol de usuario actualizado.") },
                        onFailure = { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Error al actualizar rol.") }
                    )
                }
                .catch { e -> _sharingState.value = _sharingState.value.copy(error = e.message ?: "Excepción al actualizar rol.") }
                .launchIn(viewModelScope)
        }
    }

    fun consumeSnackbarMessage() {
        _sharingState.value = _sharingState.value.copy(snackbarMessage = null)
    }
}

