package com.example.personalfinanceapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.personalfinanceapp.ui.viewmodel.GmailIngestionAccountState
import com.example.personalfinanceapp.ui.viewmodel.IngestionAccountStatus
import com.example.personalfinanceapp.ui.viewmodel.SettingsViewModel

/**
 * Pantalla de Ajustes para gestionar cuentas y otras configuraciones.
 * Incluye la gestión de la cuenta de Gmail para ingesta de correos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val ingestionState by viewModel.ingestionAccountState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar para errores o mensajes informativos
    LaunchedEffect(ingestionState.error) {
        ingestionState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.consumeError() // Limpiar el error después de mostrarlo
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección de Cuenta Principal (Placeholder - solo visualización)
            AccountInfoCard(
                title = "Cuenta Principal (Autenticación y Drive)",
                // En una app real, este email vendría del AuthViewModel o similar
                email = "usuario.principal@example.com", 
                icon = Icons.Filled.Email,
                statusText = "Conectada (para respaldos y app)",
                statusColor = MaterialTheme.colorScheme.secondary
            )

            // Sección de Cuenta de Gmail para Ingesta
            GmailIngestionAccountCard(state = ingestionState, viewModel = viewModel)

            // Diálogos de confirmación
            if (ingestionState.showLinkConfirmationDialog) {
                ConfirmationDialog(
                    title = "Cambiar Cuenta de Ingesta",
                    text = "Esto reemplazará la cuenta de ingesta actual (${ingestionState.linkedEmail}). Los gastos ya importados se mantendrán. ¿Continuar?",
                    onConfirm = { viewModel.confirmLinkOrChangeAccount() },
                    onDismiss = { viewModel.cancelLinkOrChangeAccount() }
                )
            }

            if (ingestionState.showUnlinkConfirmationDialog) {
                ConfirmationDialog(
                    title = "Desvincular Cuenta de Ingesta",
                    text = "¿Estás seguro de que deseas desvincular la cuenta ${ingestionState.linkedEmail}? Los gastos importados se mantendrán. No se procesarán nuevos correos hasta que vincules otra cuenta.",
                    onConfirm = { viewModel.confirmUnlinkAccount() },
                    onDismiss = { viewModel.cancelUnlinkAccount() }
                )
            }
        }
    }
}

@Composable
fun AccountInfoCard(
    title: String,
    email: String?,
    icon: ImageVector,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    actions: @Composable (() -> Unit)? = null
) {
    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(email ?: "No vinculada", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
            actions?.let {
                Spacer(modifier = Modifier.height(12.dp))
                it()
            }
        }
    }
}

@Composable
fun GmailIngestionAccountCard(
    state: GmailIngestionAccountState,
    viewModel: SettingsViewModel
) {
    val statusText:
    String
    val statusColor: androidx.compose.ui.graphics.Color
    val buttonText: String
    val buttonIcon: ImageVector
    val onButtonClick: () -> Unit

    when (state.accountStatus) {
        IngestionAccountStatus.NOT_LINKED -> {
            statusText = "Ninguna cuenta de Gmail vinculada para la ingesta de correos."
            statusColor = MaterialTheme.colorScheme.onSurfaceVariant
            buttonText = "Vincular Cuenta de Gmail"
            buttonIcon = Icons.Filled.Link
            onButtonClick = { viewModel.onLinkOrChangeAccountClicked() }
        }
        IngestionAccountStatus.LINKED -> {
            statusText = "Conectada y operativa para ingesta."
            statusColor = MaterialTheme.colorScheme.secondary // Verde o color de éxito
            buttonText = "Cambiar Cuenta"
            buttonIcon = Icons.Filled.Link // O un icono de "cambiar"
            onButtonClick = { viewModel.onLinkOrChangeAccountClicked() }
        }
        IngestionAccountStatus.REVOKED -> {
            statusText = "Permisos revocados. La ingesta de correos está detenida."
            statusColor = MaterialTheme.colorScheme.error
            buttonText = "Re-autorizar Cuenta"
            buttonIcon = Icons.Filled.SyncLock
            onButtonClick = { viewModel.onReauthorizeClicked() }
        }
        IngestionAccountStatus.ERROR -> {
            statusText = "Error con la cuenta de ingesta. Revisa los detalles."
            statusColor = MaterialTheme.colorScheme.error
            buttonText = "Intentar Vincular de Nuevo"
            buttonIcon = Icons.Filled.Link
            onButtonClick = { viewModel.onLinkOrChangeAccountClicked() } // O una acción específica de reintento
        }
    }

    AccountInfoCard(
        title = "Cuenta de Gmail para Ingesta de Correos",
        email = state.linkedEmail,
        icon = Icons.Filled.Email,
        statusText = statusText,
        statusColor = statusColor,
        actions = {
            Column {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = onButtonClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(buttonText)
                    }
                    if (state.accountStatus == IngestionAccountStatus.LINKED || state.accountStatus == IngestionAccountStatus.REVOKED || state.accountStatus == IngestionAccountStatus.ERROR && state.linkedEmail != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.onUnlinkAccountClicked() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Desvincular Cuenta")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

