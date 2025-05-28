package com.example.personalfinanceapp.data.remote

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject

/**
 * Implementación del DataSource de Gmail.
 * Utiliza la API de Gmail para obtener correos electrónicos usando un token de acceso específico.
 */
class GmailDataSourceImpl @Inject constructor(private val context: Context) : GmailDataSource {

    companion object {
        private const val TAG = "GmailDataSourceImpl"
    }

    /**
     * Crea una instancia del servicio de Gmail utilizando el token de acceso proporcionado
     * para la cuenta de ingesta especificada.
     *
     * @param accessToken El token de acceso OAuth2 para la cuenta de Gmail de ingesta.
     * @param ingestionAccountEmail El email de la cuenta de Gmail (no se usa directamente para crear la credencial con token, pero es bueno para logging o referencia).
     * @return Una instancia de Gmail service, o null si ocurre un error.
     */
    private fun getGmailService(accessToken: String, ingestionAccountEmail: String): Gmail? {
        return try {
            Log.d(TAG, "Creando servicio de Gmail para: $ingestionAccountEmail con token proporcionado.")
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(GmailScopes.GMAIL_READONLY)
            )
            credential.token = accessToken // Establecer el token de acceso directamente
            // No es necesario setSelectedAccountName si ya tenemos el token para esa cuenta.

            Gmail.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
            .setApplicationName("PersonalBudget") // Nombre de la aplicación
            .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear el servicio de Gmail para $ingestionAccountEmail: ${e.message}", e)
            null
        }
    }

    override suspend fun listEmails(
        accessToken: String,
        ingestionAccountEmail: String, // Usado como "userId" en la API de Gmail
        query: String
    ): Flow<List<EmailData>> = flow {
        val service = getGmailService(accessToken, ingestionAccountEmail)
        if (service == null) {
            Log.w(TAG, "Servicio de Gmail no disponible para listar correos de $ingestionAccountEmail.")
            emit(emptyList()) // O emitir un error específico
            return@flow
        }
        try {
            Log.d(TAG, "Listando correos para $ingestionAccountEmail con query: $query")
            // "me" se puede usar si el token de acceso corresponde a la cuenta que se está consultando.
            // O se puede usar ingestionAccountEmail directamente si la API lo permite para tokens delegados.
            // Generalmente, con un token OAuth2, "me" se refiere a la cuenta propietaria del token.
            val response = service.users().messages().list(ingestionAccountEmail).setQ(query).execute()
            val messages = response.messages ?: emptyList()
            val emailDataList = mutableListOf<EmailData>()

            Log.d(TAG, "Se encontraron ${messages.size} mensajes para $ingestionAccountEmail.")

            for (message in messages) {
                getEmailContent(accessToken, ingestionAccountEmail, message.id).collect { detailedMessage -> 
                    detailedMessage?.let { emailDataList.add(it) }
                }
            }
            emit(emailDataList)
        } catch (e: IOException) {
            Log.e(TAG, "IOException al listar correos para $ingestionAccountEmail: ${e.message}", e)
            emit(emptyList()) // O emitir un error específico
        } catch (e: Exception) {
            Log.e(TAG, "Excepción general al listar correos para $ingestionAccountEmail: ${e.message}", e)
            emit(emptyList()) // O emitir un error específico
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getEmailContent(
        accessToken: String,
        ingestionAccountEmail: String, // Usado como "userId" en la API de Gmail
        messageId: String
    ): Flow<EmailData?> = flow {
        val service = getGmailService(accessToken, ingestionAccountEmail)
        if (service == null) {
            Log.w(TAG, "Servicio de Gmail no disponible para obtener contenido del mensaje $messageId de $ingestionAccountEmail.")
            emit(null) // O emitir un error específico
            return@flow
        }
        try {
            Log.d(TAG, "Obteniendo contenido del mensaje $messageId para $ingestionAccountEmail")
            val message: Message = service.users().messages().get(ingestionAccountEmail, messageId).setFormat("full").execute()
            
            var bodyHtml: String? = null
            var bodyText: String? = null
            var fromHeader: String? = null
            var subjectHeader: String? = null

            message.payload?.headers?.forEach { header ->
                when (header.name?.lowercase()) {
                    "from" -> fromHeader = header.value
                    "subject" -> subjectHeader = header.value
                }
            }
            
            // Lógica de extracción de cuerpo (simplificada, necesita ser robusta para multipart)
            fun extractBody(payload: com.google.api.services.gmail.model.MessagePart): Pair<String?, String?> {
                var html: String? = null
                var text: String? = null
                if (payload.mimeType == "text/html") {
                    payload.body?.data?.let {
                        html = String(android.util.Base64.decode(it, android.util.Base64.URL_SAFE), Charsets.UTF_8)
                    }
                } else if (payload.mimeType == "text/plain") {
                    payload.body?.data?.let {
                        text = String(android.util.Base64.decode(it, android.util.Base64.URL_SAFE), Charsets.UTF_8)
                    }
                } else if (payload.mimeType?.startsWith("multipart/") == true) {
                    payload.parts?.forEach { part ->
                        val (partHtml, partText) = extractBody(part)
                        if (html == null) html = partHtml
                        if (text == null) text = partText
                        if (html != null && text != null) return@forEach // Priorizar si ya se encontraron ambos
                    }
                }
                return Pair(html, text)
            }

            message.payload?.let {
                val (h, t) = extractBody(it)
                bodyHtml = h
                bodyText = t
            }

            emit(
                EmailData(
                    id = message.id,
                    threadId = message.threadId,
                    snippet = message.snippet,
                    bodyHtml = bodyHtml,
                    bodyText = bodyText,
                    from = fromHeader,
                    subject = subjectHeader,
                    date = message.internalDate 
                )
            )
        } catch (e: IOException) {
            Log.e(TAG, "IOException al obtener contenido del mensaje $messageId para $ingestionAccountEmail: ${e.message}", e)
            emit(null) 
        } catch (e: Exception) {
            Log.e(TAG, "Excepción general al obtener contenido del mensaje $messageId para $ingestionAccountEmail: ${e.message}", e)
            emit(null) 
        }
    }.flowOn(Dispatchers.IO)
}

