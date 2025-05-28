package com.example.personalfinanceapp.data.repository

import android.util.Log
import com.example.personalfinanceapp.data.local.AppDatabase // Para acceder a los DAOs de gastos, etc.
import com.example.personalfinanceapp.data.local.sharing.SharedUserDao
import com.example.personalfinanceapp.data.local.sharing.SharedUserEntity
import com.example.personalfinanceapp.data.local.sharing.SharingInvitationDao
import com.example.personalfinanceapp.data.local.sharing.SharingInvitationEntity
import com.example.personalfinanceapp.data.remote.DriveDataSource
import com.example.personalfinanceapp.domain.model.sharing.InvitationStatus
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import com.example.personalfinanceapp.domain.repository.SharingRepository
import com.example.personalfinanceapp.worker.DriveBackupWorker // Para la constante BACKUP_FOLDER_NAME
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio para la compartición de datos.
 * Maneja la lógica de invitaciones, usuarios compartidos y sincronización de datos con Google Drive.
 */
@Singleton
class SharingRepositoryImpl @Inject constructor(
    private val sharedUserDao: SharedUserDao,
    private val sharingInvitationDao: SharingInvitationDao,
    private val driveDataSource: DriveDataSource,
    private val appDatabase: AppDatabase, // Para acceder a los datos locales a fusionar/exportar
    private val gson: Gson
) : SharingRepository {

    companion object {
        private const val TAG = "SharingRepositoryImpl"
    }

    override suspend fun sendInvitation(
        invitedUserEmail: String,
        role: UserRole,
        inviterDriveFileId: String // El ID del archivo JSON del invitador en su Drive
    ): Flow<Result<SharingInvitationEntity>> = flow {
        try {
            Log.d(TAG, "Enviando invitación a $invitedUserEmail con rol $role y archivo $inviterDriveFileId")
            // Aquí, en una app real, se notificaría al otro usuario (ej. vía email o un sistema de backend).
            // Por ahora, solo creamos el registro local de la invitación.
            val invitationId = UUID.randomUUID().toString()
            val currentUserEmail = "me@example.com" // Placeholder: obtener el email del usuario actual

            val invitation = SharingInvitationEntity(
                invitationId = invitationId,
                invitedUserEmail = invitedUserEmail,
                inviterUserEmail = currentUserEmail, // Asumir que "me" es el invitador
                requestedRole = role,
                status = InvitationStatus.PENDING.name,
                inviterDriveFileId = inviterDriveFileId,
                direction = "SENT"
            )
            sharingInvitationDao.insertInvitation(invitation)
            Log.d(TAG, "Invitación enviada (localmente): ${invitation.invitationId}")
            emit(Result.success(invitation))
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar invitación: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun acceptInvitation(invitationId: String): Flow<Result<SharedUserEntity>> = flow {
        try {
            val invitation = sharingInvitationDao.getInvitationById(invitationId).firstOrNull()
            if (invitation == null || invitation.status != InvitationStatus.PENDING.name) {
                emit(Result.failure(IllegalStateException("Invitación no válida o ya procesada.")))
                return@flow
            }

            // Actualizar estado de la invitación
            sharingInvitationDao.updateInvitation(invitation.copy(status = InvitationStatus.ACCEPTED.name))

            // Agregar al invitador a la lista de usuarios compartidos
            val sharedUser = SharedUserEntity(
                userId = invitation.inviterUserEmail, // El email del invitador es el ID
                email = invitation.inviterUserEmail,
                roleGivenByMe = null, // Yo no le di rol a él sobre mis datos (aún)
                theirDriveFileId = invitation.inviterDriveFileId, // El ID del archivo del invitador
                myRoleForTheirData = invitation.requestedRole, // El rol que tengo sobre los datos del invitador
                lastSyncTimestamp = null
            )
            sharedUserDao.insertSharedUser(sharedUser)
            Log.d(TAG, "Invitación ${invitation.invitationId} aceptada. Usuario ${sharedUser.email} agregado.")
            emit(Result.success(sharedUser))
            // Opcionalmente, iniciar una primera sincronización aquí
            // syncDataFromSharedUser(sharedUser).collect() // Cuidado con el anidamiento de flows

        } catch (e: Exception) {
            Log.e(TAG, "Error al aceptar invitación: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun rejectInvitation(invitationId: String): Flow<Result<Unit>> = flow {
        try {
            val invitation = sharingInvitationDao.getInvitationById(invitationId).firstOrNull()
            if (invitation == null || invitation.status != InvitationStatus.PENDING.name) {
                emit(Result.failure(IllegalStateException("Invitación no válida o ya procesada.")))
                return@flow
            }
            sharingInvitationDao.updateInvitation(invitation.copy(status = InvitationStatus.REJECTED.name))
            Log.d(TAG, "Invitación ${invitation.invitationId} rechazada.")
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Error al rechazar invitación: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getReceivedInvitations(userEmail: String): Flow<List<SharingInvitationEntity>> {
        return sharingInvitationDao.getReceivedInvitationsForUser(userEmail)
    }

    override fun getSentInvitations(userEmail: String): Flow<List<SharingInvitationEntity>> {
        return sharingInvitationDao.getSentInvitationsByUser(userEmail)
    }

    override suspend fun addSharedUser(
        email: String,
        role: UserRole,
        theirDriveFileId: String,
        myRoleForTheirData: UserRole
    ): Flow<Result<SharedUserEntity>> = flow {
        // Este método podría ser usado si se agrega un usuario compartido directamente sin invitación
        // O como parte del flujo de aceptación de invitación.
        try {
            val sharedUser = SharedUserEntity(
                userId = email,
                email = email,
                roleGivenByMe = role, // Rol que yo le doy a este usuario sobre mis datos
                theirDriveFileId = theirDriveFileId, // ID del archivo de este usuario en SU Drive
                myRoleForTheirData = myRoleForTheirData, // Rol que tengo yo sobre los datos de este usuario
                lastSyncTimestamp = null
            )
            sharedUserDao.insertSharedUser(sharedUser)
            emit(Result.success(sharedUser))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateSharedUserRole(userId: String, newRole: UserRole): Flow<Result<Unit>> = flow {
        try {
            val user = sharedUserDao.getSharedUserById(userId).firstOrNull()
            if (user != null) {
                sharedUserDao.updateSharedUser(user.copy(roleGivenByMe = newRole))
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Usuario no encontrado")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun removeSharedUser(userId: String): Flow<Result<Unit>> = flow {
        try {
            sharedUserDao.deleteSharedUser(userId)
            // También se podrían eliminar invitaciones relacionadas si es necesario
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSharedUser(userId: String): Flow<SharedUserEntity?> {
        return sharedUserDao.getSharedUserById(userId)
    }

    override fun getAllSharedUsers(): Flow<List<SharedUserEntity>> {
        return sharedUserDao.getAllSharedUsers()
    }

    override suspend fun syncDataFromSharedUser(sharedUser: SharedUserEntity): Flow<Result<Unit>> = flow {
        if (sharedUser.theirDriveFileId == null) {
            emit(Result.failure(IllegalArgumentException("El usuario compartido no tiene un ID de archivo de Drive.")))
            return@flow
        }
        Log.d(TAG, "Iniciando sincronización desde ${sharedUser.email} (archivo: ${sharedUser.theirDriveFileId})")
        try {
            // 1. Descargar el archivo JSON desde Drive
            val tempFile = File.createTempFile("shared_data_", ".json")
            driveDataSource.downloadFile(sharedUser.theirDriveFileId, tempFile).firstOrNull()
                ?: throw Exception("Error al descargar el archivo de Drive")

            // 2. Leer y parsear el JSON
            val backupData: com.example.personalfinanceapp.worker.DatabaseBackup = FileReader(tempFile).use { reader ->
                gson.fromJson(reader, com.example.personalfinanceapp.worker.DatabaseBackup::class.java)
            }
            tempFile.delete()

            // 3. Fusionar los datos en la base de datos local
            // Aquí se necesita una estrategia de fusión cuidadosa para evitar duplicados y manejar conflictos.
            // Por simplicidad, usaremos OnConflictStrategy.REPLACE o IGNORE donde sea apropiado.
            // IMPORTANTE: Esta lógica de fusión es un placeholder y debe ser robusta.
            // Deberías considerar IDs únicos globales o una lógica de "upsert" más inteligente.

            backupData.categories.forEach { category ->
                // Antes de insertar, podrías verificar si ya existe una categoría con el mismo nombre
                // y decidir si actualizarla o ignorar la nueva.
                // Por ahora, asumimos que el ID es único o que el DAO maneja el conflicto.
                appDatabase.categoryDao().insertCategory(category) // Asume que CategoryEntity es el mismo tipo
            }
            backupData.budgets.forEach { budget ->
                appDatabase.budgetDao().insertBudget(budget)
            }
            backupData.expenses.forEach { expense ->
                // Para los gastos, la fusión es más crítica. Se necesita evitar duplicados.
                // Podrías usar una combinación de fecha, monto, comercio para identificar duplicados.
                // O si los gastos tienen IDs únicos globales, sería más fácil.
                appDatabase.expenseDao().insertExpense(expense)
            }

            // Actualizar el timestamp de la última sincronización
            sharedUserDao.updateSharedUser(sharedUser.copy(lastSyncTimestamp = System.currentTimeMillis()))
            Log.d(TAG, "Sincronización desde ${sharedUser.email} completada.")
            emit(Result.success(Unit))

        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar datos desde ${sharedUser.email}: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Asegura que los datos del usuario actual estén disponibles para compartir en Drive.
     * Esto implica ejecutar una lógica similar al DriveBackupWorker para crear/actualizar el JSON.
     * Devuelve el ID del archivo de Drive del usuario actual.
     */
    override suspend fun ensureMyDataIsSharable(): Flow<Result<String?>> = flow {
        Log.d(TAG, "Asegurando que los datos propios sean compartibles...")
        try {
            val expenses = appDatabase.expenseDao().getAllExpenses().firstOrNull() ?: emptyList()
            val categories = appDatabase.categoryDao().getAllCategories().firstOrNull() ?: emptyList()
            val budgets = appDatabase.budgetDao().getAllBudgets().firstOrNull() ?: emptyList()
            val backupData = com.example.personalfinanceapp.worker.DatabaseBackup(expenses, categories, budgets)
            val jsonBackup = gson.toJson(backupData)

            val localFileName = "my_personalbudget_data.json" // Nombre de archivo fijo para el usuario actual
            val localFile = File(appDatabase.openHelper.writableDatabase.path.removeSuffix("personal-finance.db") + "cache/" + localFileName) // Usar cacheDir
            localFile.parentFile?.mkdirs()
            FileWriter(localFile).use { writer -> writer.write(jsonBackup) }

            var folderId: String? = null
            driveDataSource.getOrCreateAppFolder(DriveBackupWorker.BACKUP_FOLDER_NAME).collect { id -> folderId = id }
            if (folderId == null) throw Exception("No se pudo obtener/crear carpeta de Drive")

            // Buscar si el archivo ya existe para actualizarlo, en lugar de crear uno nuevo siempre.
            // Esta lógica de búsqueda y actualización es simplificada aquí.
            // Idealmente, se guardaría el ID del archivo propio en SharedPreferences o Room.
            var existingFileId: String? = null 
            // driveDataSource.findFileByName(localFileName, folderId).firstOrNull()?.let { existingFileId = it.id }

            var uploadedFileId: String? = null
            if (existingFileId != null) {
                // driveDataSource.updateFile(existingFileId, localFileName, localFile, "application/json").collect { fileId -> uploadedFileId = fileId }
                // Por ahora, siempre creamos uno nuevo para simplificar, pero esto no es ideal para "actualizar"
                 driveDataSource.uploadFile(localFileName, localFile, "application/json", folderId).collect{ fileId -> uploadedFileId = fileId}
            } else {
                driveDataSource.uploadFile(localFileName, localFile, "application/json", folderId).collect{ fileId -> uploadedFileId = fileId}
            }
            
            localFile.delete()
            if (uploadedFileId != null) {
                Log.d(TAG, "Datos propios listos para compartir. File ID: $uploadedFileId")
                emit(Result.success(uploadedFileId))
            } else {
                emit(Result.failure(Exception("Error al subir/actualizar archivo de datos propios a Drive.")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al asegurar datos compartibles: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

