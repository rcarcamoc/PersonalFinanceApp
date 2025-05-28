package com.example.personalfinanceapp.domain.model.sharing

/**
 * Define los roles de usuario para la compartición de datos.
 */
enum class UserRole {
    READER, // Solo puede ver resúmenes y reportes
    WRITER  // Puede agregar/editar registros y presupuestos
}

/**
 * Modelo para representar la información de un usuario con el que se comparten datos.
 *
 * @property userId El ID único del usuario (podría ser el email o un ID de Google).
 * @property email El email del usuario invitado.
 * @property role El rol asignado al usuario.
 * @property driveFileId El ID del archivo JSON de este usuario en su Google Drive (si aplica, para que otros lo descarguen).
 * @property sharedWithMeDriveFileId El ID del archivo JSON del usuario que compartió sus datos conmigo (si aplica).
 */
data class SharedUser(
    val userId: String,
    val email: String,
    val role: UserRole,
    val driveFileId: String? = null, // ID del archivo JSON de *este* usuario en su Drive
    // Información sobre los datos que otros comparten conmigo
    val sharedByUsers: List<SharedSourceInfo> = emptyList()
)

/**
 * Información sobre una fuente de datos compartida por otro usuario.
 *
 * @property sharerEmail Email del usuario que comparte.
 * @property sharerDriveFileId ID del archivo JSON del usuario que comparte en su Drive.
 * @property myRoleAsCollaborator Mi rol respecto a los datos de este sharer.
 */
data class SharedSourceInfo(
    val sharerEmail: String,
    val sharerDriveFileId: String,
    val myRoleAsCollaborator: UserRole // Qué puedo hacer con los datos de esta persona
)

/**
 * Modelo para una invitación pendiente.
 *
 * @property invitationId ID único de la invitación.
 * @property invitedUserEmail Email del usuario invitado.
 * @property inviterUserEmail Email del usuario que invita.
 * @property requestedRole Rol que se ofrece al invitado.
 * @property status Estado de la invitación (ej. PENDING, ACCEPTED, REJECTED).
 * @property inviterDriveFileId El ID del archivo JSON del invitador en su Drive, para que el invitado pueda acceder.
 */
data class SharingInvitation(
    val invitationId: String,
    val invitedUserEmail: String,
    val inviterUserEmail: String,
    val requestedRole: UserRole,
    var status: InvitationStatus = InvitationStatus.PENDING,
    val inviterDriveFileId: String // El ID del archivo del invitador que el invitado necesita leer
)

enum class InvitationStatus {
    PENDING, ACCEPTED, REJECTED
}

