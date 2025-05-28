package com.example.personalfinanceapp.data.remote

import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Interfaz para el DataSource de Google Drive.
 * Define los métodos para interactuar con la API de Google Drive,
 * específicamente para subir y descargar archivos (ej. backups en JSON).
 */
interface DriveDataSource {
    /**
     * Sube un archivo a una carpeta específica en Google Drive.
     *
     * @param fileName El nombre que tendrá el archivo en Drive.
     * @param localFile El archivo local que se va a subir.
     * @param mimeType El tipo MIME del archivo (ej. "application/json").
     * @param folderId El ID de la carpeta en Drive donde se subirá el archivo (opcional, si no se especifica se usa la raíz o una carpeta por defecto).
     * @return Un Flow que emite el ID del archivo subido en Drive o un error.
     */
    suspend fun uploadFile(fileName: String, localFile: File, mimeType: String, folderId: String? = null): Flow<String?>

    /**
     * Descarga un archivo desde Google Drive.
     *
     * @param fileId El ID del archivo en Drive que se va a descargar.
     * @param destinationFile El archivo local donde se guardará el contenido descargado.
     * @return Un Flow que emite true si la descarga fue exitosa, false o un error en caso contrario.
     */
    suspend fun downloadFile(fileId: String, destinationFile: File): Flow<Boolean>

    /**
     * Lista los archivos en una carpeta específica de Google Drive.
     * Podría usarse para encontrar backups anteriores.
     *
     * @param folderId El ID de la carpeta en Drive.
     * @param query Un filtro adicional para los archivos (ej. "mimeType='application/json' and name contains 'backup'").
     * @return Un Flow que emite una lista de metadatos de archivos o un error.
     */
    suspend fun listFilesInFolder(folderId: String, query: String? = null): Flow<List<DriveFileMetadata>>

    /**
     * Busca o crea una carpeta dedicada para la aplicación en Google Drive.
     * @param folderName El nombre de la carpeta a buscar o crear.
     * @return Un Flow que emite el ID de la carpeta.
     */
    suspend fun getOrCreateAppFolder(folderName: String): Flow<String?>
}

/**
 * Modelo de datos para representar metadatos de un archivo en Google Drive.
 */
data class DriveFileMetadata(
    val id: String,
    val name: String,
    val mimeType: String,
    val modifiedTime: Long? // Timestamp de la última modificación
)

