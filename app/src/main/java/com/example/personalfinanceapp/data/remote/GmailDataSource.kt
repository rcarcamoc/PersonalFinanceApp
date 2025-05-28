package com.example.personalfinanceapp.data.remote

import kotlinx.coroutines.flow.Flow

/**
 * Interface para el DataSource de Gmail.
 */
interface GmailDataSource {
    /**
     * Lista los correos electrónicos que coinciden con la consulta para una cuenta de Gmail específica.
     *
     * @param accessToken El token de acceso OAuth2 para la cuenta de Gmail de ingesta.
     * @param ingestionAccountEmail El email de la cuenta de Gmail desde la cual leer los correos (usado como 'userId' para la API de Gmail).
     * @param query La consulta para filtrar los correos (ej. "from:banco@example.com subject:Notificación").
     * @return Un Flow que emite una lista de EmailData o una lista vacía/error en caso de fallo.
     */
    suspend fun listEmails(
        accessToken: String,
        ingestionAccountEmail: String,
        query: String
    ): Flow<List<EmailData>>

    /**
     * Obtiene el contenido detallado de un correo electrónico específico de una cuenta de Gmail.
     *
     * @param accessToken El token de acceso OAuth2 para la cuenta de Gmail de ingesta.
     * @param ingestionAccountEmail El email de la cuenta de Gmail (usado como 'userId').
     * @param messageId El ID del mensaje de Gmail a obtener.
     * @return Un Flow que emite EmailData o null/error si no se encuentra o falla.
     */
    suspend fun getEmailContent(
        accessToken: String,
        ingestionAccountEmail: String,
        messageId: String
    ): Flow<EmailData?>
}

