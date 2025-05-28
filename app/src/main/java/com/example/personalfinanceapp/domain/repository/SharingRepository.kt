package com.example.personalfinanceapp.domain.repository

import com.example.personalfinanceapp.data.local.sharing.SharedUserEntity
import com.example.personalfinanceapp.data.local.sharing.SharingInvitationEntity
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de compartición de datos.
 * Define los métodos para gestionar invitaciones, usuarios compartidos y sincronización de datos.
 */
interface SharingRepository {

    // Gestión de Invitaciones
    suspend fun sendInvitation(invitedUserEmail: String, role: UserRole, inviterDriveFileId: String): Flow<Result<SharingInvitationEntity>>
    suspend fun acceptInvitation(invitationId: String): Flow<Result<SharedUserEntity>>
    suspend fun rejectInvitation(invitationId: String): Flow<Result<Unit>>
    fun getReceivedInvitations(userEmail: String): Flow<List<SharingInvitationEntity>>
    fun getSentInvitations(userEmail: String): Flow<List<SharingInvitationEntity>>

    // Gestión de Usuarios Compartidos
    suspend fun addSharedUser(email: String, role: UserRole, theirDriveFileId: String, myRoleForTheirData: UserRole): Flow<Result<SharedUserEntity>>
    suspend fun updateSharedUserRole(userId: String, newRole: UserRole): Flow<Result<Unit>>
    suspend fun removeSharedUser(userId: String): Flow<Result<Unit>>
    fun getSharedUser(userId: String): Flow<SharedUserEntity?>
    fun getAllSharedUsers(): Flow<List<SharedUserEntity>>

    // Sincronización de Datos Compartidos
    /**
     * Descarga el archivo JSON de un usuario compartido y fusiona los datos en la base de datos local.
     * @param sharedUser El usuario cuyos datos se van a descargar y fusionar.
     * @return Flow que emite el resultado de la operación (éxito/fallo).
     */
    suspend fun syncDataFromSharedUser(sharedUser: SharedUserEntity): Flow<Result<Unit>>

    /**
     * Prepara y potencialmente sube (o actualiza) el archivo JSON del usuario actual para que otros lo sincronicen.
     * Esto podría ser parte del worker de respaldo o una operación separada.
     * @return Flow que emite el ID del archivo de Drive del usuario actual o un error.
     */
    suspend fun ensureMyDataIsSharable(): Flow<Result<String?>>
}

