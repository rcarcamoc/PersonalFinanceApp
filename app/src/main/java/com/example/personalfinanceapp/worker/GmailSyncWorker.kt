package com.example.personalfinanceapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.domain.repository.ExpenseRepository // O los UseCases específicos
import com.example.personalfinanceapp.data.remote.GmailDataSource
import com.example.personalfinanceapp.data.remote.EmailData
// Importar clases para obtener preferencias de cuenta de ingesta y token
// import com.example.personalfinanceapp.domain.usecase.auth.GetIngestionAccountDetailsUseCase // Ejemplo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup // Para parsear HTML
import java.text.SimpleDateFormat
import java.util.Locale

@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val gmailDataSource: GmailDataSource,
    private val expenseRepository: ExpenseRepository,
    // Inyectar el UseCase o Repositorio para obtener los detalles de la cuenta de ingesta
    // private val getIngestionAccountDetailsUseCase: GetIngestionAccountDetailsUseCase // Ejemplo
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "GmailSyncWorker"
        private const val TAG = "GmailSyncWorker"
        const val GMAIL_QUERY = "from:contacto@bci.cl subject:\"Notificación de uso de tu tarjeta de crédito\"" // Esto debería ser configurable
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando sincronización de correos de Gmail...")

        // 1. Obtener los detalles de la cuenta de Gmail para ingesta (email y token)
        // Esta es una simulación. En una implementación real, se usaría un UseCase/Repository.
        val ingestionAccountEmail = getStoredIngestionAccountEmail() // Implementar esta función
        val accessToken = getStoredAccessTokenForIngestion()     // Implementar esta función

        if (ingestionAccountEmail == null || accessToken == null) {
            Log.w(TAG, "No se encontró una cuenta de Gmail para ingesta configurada o token no disponible. Abortando sincronización.")
            // Podrías querer notificar al usuario o cambiar el estado de la cuenta a "requiere configuración/re-autorización"
            return@withContext Result.failure() // O success si se considera que no es un fallo del worker en sí
        }

        Log.d(TAG, "Usando cuenta de ingesta: $ingestionAccountEmail")

        try {
            val emails = gmailDataSource.listEmails(accessToken, ingestionAccountEmail, GMAIL_QUERY).firstOrNull()
            
            if (emails == null) {
                 Log.e(TAG, "Error al listar correos. El DataSource devolvió null.")
                 // Aquí se podría manejar la revocación de token si el error específico lo indica
                 // Por ejemplo, si el DataSource lanza una excepción específica para token inválido.
                 // updateIngestionAccountStatusToRevoked() // Implementar esta función
                 return@withContext Result.failure()
            }

            if (emails.isEmpty()) {
                Log.d(TAG, "No se encontraron correos nuevos que coincidan con la query para $ingestionAccountEmail.")
            } else {
                Log.d(TAG, "Se encontraron ${emails.size} correos para procesar.")
            }

            for (emailData in emails) {
                Log.d(TAG, "Procesando correo ID: ${emailData.id} de $ingestionAccountEmail")
                val expense = parseExpenseFromEmail(emailData)
                if (expense != null) {
                    // Considerar verificar duplicados antes de insertar
                    expenseRepository.insertExpense(expense)
                    Log.d(TAG, "Gasto insertado: ${expense.merchant} - ${expense.amount}")
                } else {
                    Log.w(TAG, "No se pudo parsear el gasto del correo ID: ${emailData.id}")
                }
            }
            Log.d(TAG, "Sincronización de correos de Gmail completada para $ingestionAccountEmail.")
            Result.success()
        } catch (e: Exception) {
            // Aquí se podría verificar si la excepción es por token inválido/revocado
            // y actuar en consecuencia (ej. notificar, cambiar estado de cuenta)
            Log.e(TAG, "Error durante la sincronización de Gmail para $ingestionAccountEmail: ${e.message}", e)
            // if (e is TokenRevokedException) { updateIngestionAccountStatusToRevoked() }
            Result.failure()
        }
    }

    // --- Funciones Placeholder para obtener email y token (DEBEN SER IMPLEMENTADAS) ---
    private suspend fun getStoredIngestionAccountEmail(): String? {
        // Lógica para obtener el email de la cuenta de ingesta desde SharedPreferences
        // Ejemplo: return preferencesManager.getIngestionEmail()
        Log.w(TAG, "getStoredIngestionAccountEmail() ES SIMULADO - IMPLEMENTAR")
        return "ingestion.account.test@gmail.com" // Placeholder
    }

    private suspend fun getStoredAccessTokenForIngestion(): String? {
        // Lógica para obtener el token de acceso desde EncryptedSharedPreferences
        // Ejemplo: return tokenManager.getIngestionAccessToken()
        // Considerar manejo de expiración y refresco de token aquí o en un manager dedicado.
        Log.w(TAG, "getStoredAccessTokenForIngestion() ES SIMULADO - IMPLEMENTAR")
        return "SIMULATED_ACCESS_TOKEN" // Placeholder
    }
    
    // private suspend fun updateIngestionAccountStatusToRevoked() {
    //     // Lógica para actualizar el estado de la cuenta en SharedPreferences a REVOKED
    //     // de modo que la UI pueda reflejarlo y pedir al usuario que re-autorice.
    //     Log.w(TAG, "Actualizando estado de cuenta a REVOKED (simulado).")
    // }
    // --- Fin Funciones Placeholder ---

    private fun parseExpenseFromEmail(email: EmailData): ExpenseEntity? {
        val emailBody = email.bodyHtml ?: email.bodyText ?: return null
        // Log.d(TAG, "Contenido del correo para parsear: $emailBody") 
        // Demasiado verboso para logs de producción si los correos son grandes

        try {
            val document = Jsoup.parse(emailBody)
            
            // EJEMPLO DE PARSEO - ESTOS SELECTORES SON PLACEHOLDERS
            // DEBES AJUSTARLOS AL HTML REAL DEL CORREO DE NOTIFICACIÓN BANCARIA
            val amountString = document.select("td:contains(Monto:) + td").first()?.text()?.replace("$", "")?.replace(".","")?.replace(",",".")?.trim()
            val merchantString = document.select("td:contains(Comercio:) + td").first()?.text()?.trim()
            val dateTimeString = document.select("td:contains(Fecha y Hora:) + td").first()?.text()?.trim() // ej. "25/12/2023 15:30"
            val last4Digits = document.select("td:contains(Tarjeta:) + td").first()?.text()?.replace("*","")?.trim()?.takeLast(4)
            val installmentsString = document.select("td:contains(Cuotas:) + td").first()?.text()?.trim()

            if (amountString == null || merchantString == null || dateTimeString == null) {
                Log.w(TAG, "Faltan datos clave en el correo para parsear el gasto. Email ID: ${email.id}")
                return null
            }

            val amount = amountString.toDoubleOrNull() ?: return null
            val installments = installmentsString?.let { if (it.equals("-", true) || it.equals("Sin Cuotas", true) || it.equals("0", true) ) null else it.toIntOrNull() }

            // Parsear fecha y hora
            val sdfDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val parsedDateTime = try { sdfDateTime.parse(dateTimeString) } catch (e: Exception) { null }
            
            val transactionDate = parsedDateTime?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: email.date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date(it)) } ?: "ErrorFecha"
            val transactionTime = parsedDateTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "ErrorHora"

            return ExpenseEntity(
                amount = amount,
                date = transactionDate, 
                time = transactionTime, 
                merchant = merchantString,
                categoryId = null, 
                installments = installments,
                lastCardDigits = last4Digits,
                description = "Importado: ${email.subject?.take(50)}"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error al parsear el correo ID ${email.id}: ${e.message}", e)
            return null
        }
    }
}

