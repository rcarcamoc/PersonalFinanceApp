package com.example.personalfinanceapp.ui.screens.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.personalfinanceapp.data.local.sharing.SharedUserEntity
import com.example.personalfinanceapp.data.local.sharing.SharingInvitationEntity
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import com.example.personalfinanceapp.ui.viewmodel.sharing.SharingViewModel

/**
 * Pantalla para gestionar la compartición de datos, invitaciones y sincronización.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharingScreen(
    viewModel: SharingViewModel = hiltViewModel()
) {
    val state by viewModel.sharingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar cuando haya un mensaje
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbarMessage() // Limpiar el mensaje después de mostrarlo
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar("Error: $it", duration = SnackbarDuration.Long)
            // viewModel.clearError() // Opcional: método para limpiar el error en el ViewModel
        }
    }

    var showInviteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Compartir y Sincronizar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showInviteDialog = true }) {
                Icon(Icons.Filled.Add, "Invitar Usuario")
            }
        }
    ) { innerPadding ->
        if (state.isLoading && state.sharedUsers.isEmpty() && state.receivedInvitations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Cargando datos de compartición...")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Mi ID de archivo para compartir: ${state.myDriveFileId ?: "Generando..."}", style = MaterialTheme.typography.bodySmall)
            }

            if (state.receivedInvitations.isNotEmpty()) {
                item { SectionTitle("Invitaciones Recibidas") }
                items(state.receivedInvitations) { invitation ->
                    InvitationItem(invitation = invitation, onAccept = {
                        viewModel.acceptInvitation(invitation.invitationId)
                    }, onReject = {
                        viewModel.rejectInvitation(invitation.invitationId)
                    })
                }
            }

            if (state.sharedUsers.isNotEmpty()) {
                item { SectionTitle("Usuarios Compartidos") }
                items(state.sharedUsers) { user ->
                    SharedUserItem(
                        sharedUser = user,
                        onSync = {
                            if (user.theirDriveFileId != null) {
                                viewModel.syncWithUser(user)
                            } else {
                                // viewModel.showSnackbar("Este usuario no tiene un archivo de Drive para sincronizar.")
                            }
                        },
                        onRemove = { viewModel.removeSharedUser(user.userId) },
                        onChangeRole = { newRole -> viewModel.updateUserRole(user.userId, newRole) }
                    )
                }
            }

            if (state.sentInvitations.isNotEmpty()) {
                item { SectionTitle("Invitaciones Enviadas") }
                items(state.sentInvitations) { invitation ->
                    SentInvitationItem(invitation = invitation)
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteUserDialog(
            onDismiss = { showInviteDialog = false },
            onInvite = {
                email, role -> 
                viewModel.sendInvitation(email, role)
                showInviteDialog = false
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
}

@Composable
fun InvitationItem(
    invitation: SharingInvitationEntity,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("De: ${invitation.inviterUserEmail}", style = MaterialTheme.typography.titleSmall)
            Text("Rol ofrecido: ${invitation.requestedRole}")
            Text("Estado: ${invitation.status}")
            if (invitation.status == "PENDING") {
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = onAccept) { Text("Aceptar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onReject) { Text("Rechazar") }
                }
            }
        }
    }
}

@Composable
fun SentInvitationItem(invitation: SharingInvitationEntity) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Para: ${invitation.invitedUserEmail}", style = MaterialTheme.typography.titleSmall)
            Text("Rol ofrecido: ${invitation.requestedRole}")
            Text("Estado: ${invitation.status}")
        }
    }
}

@Composable
fun SharedUserItem(
    sharedUser: SharedUserEntity,
    onSync: () -> Unit,
    onRemove: () -> Unit,
    onChangeRole: (UserRole) -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(sharedUser.email, style = MaterialTheme.typography.titleSmall)
            sharedUser.myRoleForTheirData?.let {
                Text("Mi rol para sus datos: $it")
            }
            sharedUser.roleGivenByMe?.let {
                Text("Rol que le di sobre mis datos: $it")
            }
            Text("Su archivo de Drive: ${sharedUser.theirDriveFileId ?: "No especificado"}")
            Text("Última sincronización: ${sharedUser.lastSyncTimestamp?.let { java.util.Date(it).toString() } ?: "Nunca"}")

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (sharedUser.theirDriveFileId != null && sharedUser.myRoleForTheirData != null) {
                    IconButton(onClick = onSync) {
                        Icon(Icons.Filled.Sync, "Sincronizar con ${sharedUser.email}")
                    }
                }
                if (sharedUser.roleGivenByMe != null) { // Solo puedo cambiar rol si yo le di acceso
                    IconButton(onClick = { showRoleDialog = true }) {
                        Icon(Icons.Filled.Edit, "Cambiar rol de ${sharedUser.email}")
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, "Dejar de compartir con ${sharedUser.email}")
                }
            }
        }
    }
    if (showRoleDialog && sharedUser.roleGivenByMe != null) {
        ChangeRoleDialog(
            currentRole = sharedUser.roleGivenByMe,
            onDismiss = { showRoleDialog = false },
            onConfirm = { newRole ->
                onChangeRole(newRole)
                showRoleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteUserDialog(
    onDismiss: () -> Unit,
    onInvite: (email: String, role: UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.READER) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invitar Usuario a Compartir") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email del usuario") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Asignar Rol:")
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                     OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) }, 
                                onClick = { 
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onInvite(email, selectedRole) },
                enabled = email.isNotBlank()
            ) {
                Text("Enviar Invitación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoleDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onConfirm: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Rol del Usuario") },
        text = {
            Column {
                Text("Selecciona el nuevo rol para este usuario sobre tus datos:")
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) }, 
                                onClick = { 
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedRole) }) {
                Text("Confirmar Cambio")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


