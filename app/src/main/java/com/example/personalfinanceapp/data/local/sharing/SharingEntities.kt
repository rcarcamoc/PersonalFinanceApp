package com.example.personalfinanceapp.data.local.sharing

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.personalfinanceapp.data.local.typeconverter.SharingTypeConverters // Necesitarás crear este TypeConverter
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import com.example.personalfinanceapp.domain.model.sharing.SharedSourceInfo

/**
 * Entidad para almacenar información sobre usuarios con los que se comparten datos
 * o que comparten datos con el usuario actual.
 *
 * @property userId El ID único del usuario (ej. email).
 * @property email El email del usuario.
 * @property role El rol que este usuario tiene sobre MIS datos (si yo le compartí).
 * @property driveFileId El ID del archivo JSON de este usuario en SU Drive (si este usuario me compartió sus datos).
 * @property sharedByUsers Lista de usuarios que me han compartido sus datos y mi rol hacia ellos.
 */
@Entity(tableName = "shared_users")
@TypeConverters(SharingTypeConverters::class)
data class SharedUserEntity(
    @PrimaryKey val userId: String, // Email del otro usuario
    val email: String,
    val roleGivenByMe: UserRole?, // Qué rol le di yo a este usuario sobre mis datos
    val theirDriveFileId: String?, // El ID del archivo de este usuario en SU Drive (si me compartió)
    val myRoleForTheirData: UserRole?, // Qué rol tengo yo sobre los datos de este usuario (si me compartió)
    val lastSyncTimestamp: Long? = null // Para saber cuándo fue la última sincronización con este usuario
    // val sharedSourcesInfo: List<SharedSourceInfo> // Esto es más complejo de modelar directamente en Room
                                                // Podría ser una tabla separada o JSON string
)

/**
 * Entidad para almacenar invitaciones de compartición (enviadas o recibidas).
 *
 * @property invitationId ID único de la invitación.
 * @property invitedUserEmail Email del usuario invitado.
 * @property inviterUserEmail Email del usuario que invita.
 * @property requestedRole Rol que se ofrece/solicita.
 * @property status Estado de la invitación (PENDING, ACCEPTED, REJECTED).
 * @property inviterDriveFileId El ID del archivo del invitador en su Drive.
 * @property direction Indica si la invitación fue ENVIADA por el usuario actual o RECIBIDA.
 */
@Entity(tableName = "sharing_invitations")
@TypeConverters(SharingTypeConverters::class)
data class SharingInvitationEntity(
    @PrimaryKey val invitationId: String,
    val invitedUserEmail: String,
    val inviterUserEmail: String,
    val requestedRole: UserRole,
    val status: String, // PENDING, ACCEPTED, REJECTED (usar String para Room, convertir desde/hacia Enum)
    val inviterDriveFileId: String, // ID del archivo JSON del invitador en su Drive
    val timestamp: Long = System.currentTimeMillis(),
    val direction: String // SENT o RECEIVED
)

