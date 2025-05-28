package com.example.personalfinanceapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personalfinanceapp.data.local.AppDatabase // Para acceder a los DAOs
import com.example.personalfinanceapp.data.remote.DriveDataSource
import com.google.gson.Gson // Para serializar a JSON
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker para realizar un respaldo de la base de datos Room en Google Drive.
 * Exporta las tablas principales (gastos, categorías, presupuestos) a un archivo JSON.
 *
 * La ejecución periódica (ej. diaria) se puede configurar al encolar este Worker,
 * pero su funcionamiento puede estar restringido en algunos entornos.
 * Se recomienda también ofrecer una opción de ejecución manual desde la UI ("Respaldar ahora").
 */
@HiltWorker
class DriveBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appDatabase: AppDatabase, // Inyectado por Hilt
    private val driveDataSource: DriveDataSource, // Inyectado por Hilt
    private val gson: Gson // Inyectado por Hilt (necesitarás proveer Gson en tu módulo Hilt)
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DriveBackupWorker"
        private const val TAG = "DriveBackupWorker"
        private const val BACKUP_FOLDER_NAME = "PersonalBudgetBackups"
        private const val BACKUP_FILE_PREFIX = "personalbudget_backup_"
        private const val BACKUP_FILE_EXTENSION = ".json"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando respaldo de la base de datos en Google Drive...")
        try {
            // 1. Obtener todos los datos de Room
            val expenses = appDatabase.expenseDao().getAllExpenses().firstOrNull() ?: emptyList() // Tomar el primer emitido o lista vacía
            val categories = appDatabase.categoryDao().getAllCategories().firstOrNull() ?: emptyList()
            val budgets = appDatabase.budgetDao().getAllBudgets().firstOrNull() ?: emptyList()

            val backupData = DatabaseBackup(expenses, categories, budgets)

            // 2. Serializar a JSON
            val jsonBackup = gson.toJson(backupData)

            // 3. Crear archivo JSON local temporalmente
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val localFileName = "${BACKUP_FILE_PREFIX}${timestamp}${BACKUP_FILE_EXTENSION}"
            val localFile = File(applicationContext.cacheDir, localFileName)
            
            FileWriter(localFile).use { writer ->
                writer.write(jsonBackup)
            }
            Log.d(TAG, "Archivo JSON de respaldo creado localmente: ${localFile.absolutePath}")

            // 4. Obtener o crear la carpeta de la app en Drive
            var folderId: String? = null
            driveDataSource.getOrCreateAppFolder(BACKUP_FOLDER_NAME).collect { id ->
                folderId = id
            }

            if (folderId == null) {
                Log.e(TAG, "No se pudo obtener o crear la carpeta de respaldo en Drive.")
                localFile.delete() // Limpiar archivo local
                return@withContext Result.failure()
            }
            Log.d(TAG, "Carpeta de respaldo en Drive (ID: $folderId) asegurada.")

            // 5. Subir el archivo JSON a Drive
            var uploadedFileId: String? = null
            driveDataSource.uploadFile(localFileName, localFile, "application/json", folderId)
                .collect{ fileId ->
                    uploadedFileId = fileId
                }

            localFile.delete() // Limpiar archivo local después de subir (o en finally)

            if (uploadedFileId != null) {
                Log.d(TAG, "Respaldo subido a Google Drive exitosamente. File ID: $uploadedFileId")
                Result.success()
            } else {
                Log.e(TAG, "Error al subir el respaldo a Google Drive.")
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error durante el respaldo en Drive: ${e.message}", e)
            Result.failure()
        }
    }
}

/**
 * Clase contenedora para los datos de la base de datos que se serializarán a JSON.
 */
data class DatabaseBackup(
    val expenses: List<com.example.personalfinanceapp.data.local.ExpenseEntity>,
    val categories: List<com.example.personalfinanceapp.data.local.CategoryEntity>,
    val budgets: List<com.example.personalfinanceapp.data.local.BudgetEntity>
)

