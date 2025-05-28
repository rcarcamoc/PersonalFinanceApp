package com.example.personalfinanceapp.domain.usecase.sharing

import com.example.personalfinanceapp.data.local.sharing.SharedUserEntity
import com.example.personalfinanceapp.data.local.sharing.SharingInvitationEntity
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import com.example.personalfinanceapp.domain.repository.SharingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para enviar una invitación de compartición.
 */
class SendSharingInvitationUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(invitedUserEmail: String, role: UserRole, inviterDriveFileId: String): Flow<Result<SharingInvitationEntity>> {
        return sharingRepository.sendInvitation(invitedUserEmail, role, inviterDriveFileId)
    }
}

/**
 * Caso de uso para aceptar una invitación de compartición.
 */
class AcceptSharingInvitationUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(invitationId: String): Flow<Result<SharedUserEntity>> {
        return sharingRepository.acceptInvitation(invitationId)
    }
}

/**
 * Caso de uso para rechazar una invitación de compartición.
 */
class RejectSharingInvitationUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(invitationId: String): Flow<Result<Unit>> {
        return sharingRepository.rejectInvitation(invitationId)
    }
}

/**
 * Caso de uso para obtener las invitaciones recibidas por un usuario.
 */
class GetReceivedInvitationsUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    operator fun invoke(userEmail: String): Flow<List<SharingInvitationEntity>> {
        return sharingRepository.getReceivedInvitations(userEmail)
    }
}

/**
 * Caso de uso para obtener las invitaciones enviadas por un usuario.
 */
class GetSentInvitationsUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    operator fun invoke(userEmail: String): Flow<List<SharingInvitationEntity>> {
        return sharingRepository.getSentInvitations(userEmail)
    }
}

/**
 * Caso de uso para obtener la lista de usuarios con los que se comparten datos.
 */
class GetSharedUsersUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    operator fun invoke(): Flow<List<SharedUserEntity>> {
        return sharingRepository.getAllSharedUsers()
    }
}

/**
 * Caso de uso para actualizar el rol de un usuario compartido.
 */
class UpdateSharedUserRoleUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(userId: String, newRole: UserRole): Flow<Result<Unit>> {
        return sharingRepository.updateSharedUserRole(userId, newRole)
    }
}

/**
 * Caso de uso para eliminar un usuario de la lista de compartidos.
 */
class RemoveSharedUserUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(userId: String): Flow<Result<Unit>> {
        return sharingRepository.removeSharedUser(userId)
    }
}

/**
 * Caso de uso para sincronizar datos desde un usuario compartido.
 */
class SyncDataFromSharedUserUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(sharedUser: SharedUserEntity): Flow<Result<Unit>> {
        return sharingRepository.syncDataFromSharedUser(sharedUser)
    }
}

/**
 * Caso de uso para asegurar que los datos del usuario actual estén disponibles para compartir.
 */
class EnsureMyDataIsSharableUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    suspend operator fun invoke(): Flow<Result<String?>> {
        return sharingRepository.ensureMyDataIsSharable()
    }
}

/**
 * Agrupa todos los casos de uso relacionados con la compartición.
 */
data class SharingUseCases @Inject constructor(
    val sendSharingInvitation: SendSharingInvitationUseCase,
    val acceptSharingInvitation: AcceptSharingInvitationUseCase,
    val rejectSharingInvitation: RejectSharingInvitationUseCase,
    val getReceivedInvitations: GetReceivedInvitationsUseCase,
    val getSentInvitations: GetSentInvitationsUseCase,
    val getSharedUsers: GetSharedUsersUseCase,
    val updateSharedUserRole: UpdateSharedUserRoleUseCase,
    val removeSharedUser: RemoveSharedUserUseCase,
    val syncDataFromSharedUser: SyncDataFromSharedUserUseCase,
    val ensureMyDataIsSharable: EnsureMyDataIsSharableUseCase
)

