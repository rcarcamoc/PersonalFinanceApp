package com.example.personalfinanceapp.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Implementación del DataSource de Google Drive.
 * Utiliza la API de Drive para subir, descargar y listar archivos.
 */
class DriveDataSourceImpl @Inject constructor(private val context: Context) : DriveDataSource {

    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE) // Permiso para archivos creados o abiertos por la app
            ).setSelectedAccount(account.account)

            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
            .setApplicationName("PersonalBudget") // Nombre de la aplicación
            .build()
        } else {
            return null
        }
    }

    override suspend fun getOrCreateAppFolder(folderName: String): Flow<String?> = flow {
        val service = getDriveService()
        if (service == null) {
            emit(null)
            return@flow
        }
        try {
            // Buscar la carpeta
            val query = "mimeType=\'application/vnd.google-apps.folder\' and name=\'$folderName\' and trashed=false"
            val result: FileList = service.files().list().setQ(query).setSpaces("drive").execute()
            if (result.files.isNotEmpty()) {
                emit(result.files[0].id)
            } else {
                // Crear la carpeta si no existe
                val folderMetadata = File()
                folderMetadata.name = folderName
                folderMetadata.mimeType = "application/vnd.google-apps.folder"
                val createdFolder = service.files().create(folderMetadata).setFields("id").execute()
                emit(createdFolder.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadFile(fileName: String, localFile: java.io.File, mimeType: String, folderId: String?): Flow<String?> = flow {
        val service = getDriveService()
        if (service == null) {
            emit(null)
            return@flow
        }
        try {
            val fileMetadata = File()
            fileMetadata.name = fileName
            folderId?.let {
                fileMetadata.parents = listOf(it)
            }

            val mediaContent = FileContent(mimeType, localFile)
            val uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            emit(uploadedFile.id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun downloadFile(fileId: String, destinationFile: java.io.File): Flow<Boolean> = flow {
        val service = getDriveService()
        if (service == null) {
            emit(false)
            return@flow
        }
        try {
            val outputStream: OutputStream = FileOutputStream(destinationFile)
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.flush()
            outputStream.close()
            emit(true)
        } catch (e: Exception) {
            e.printStackTrace()
            // Si el archivo de destino se creó pero la descarga falló, podría ser bueno eliminarlo.
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun listFilesInFolder(folderId: String, query: String?): Flow<List<DriveFileMetadata>> = flow {
        val service = getDriveService()
        if (service == null) {
            emit(emptyList())
            return@flow
        }
        try {
            var fullQuery = "'$folderId' in parents and trashed=false"
            query?.let {
                fullQuery += " and ($it)"
            }
            val result: FileList = service.files().list()
                .setQ(fullQuery)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, mimeType, modifiedTime)")
                .execute()
            
            val files = result.files?.mapNotNull { file ->
                DriveFileMetadata(
                    id = file.id ?: return@mapNotNull null,
                    name = file.name ?: "Unnamed File",
                    mimeType = file.mimeType ?: "application/octet-stream",
                    modifiedTime = file.modifiedTime?.value
                )
            } ?: emptyList()
            emit(files)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}

