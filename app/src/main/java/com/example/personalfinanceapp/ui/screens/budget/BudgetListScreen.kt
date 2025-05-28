package com.example.personalfinanceapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.personalfinanceapp.data.local.BudgetEntity
import com.example.personalfinanceapp.ui.viewmodel.BudgetListViewModel

/**
 * Pantalla que muestra una lista de presupuestos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    viewModel: BudgetListViewModel = hiltViewModel(),
    onAddBudgetClick: () -> Unit,
    onBudgetClick: (Long) -> Unit // Para navegar al detalle/edición del presupuesto
) {
    val state by viewModel.budgetListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Presupuestos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudgetClick) {
                Icon(Icons.Filled.Add, "Agregar Presupuesto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error?.let {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                }
            }

            if (!state.isLoading && state.error == null) {
                if (state.budgets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay presupuestos registrados.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.budgets) { budget ->
                            BudgetItem(budget = budget, onClick = { onBudgetClick(budget.id) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable para mostrar un único ítem de presupuesto en la lista.
 */
@Composable
fun BudgetItem(
    budget: BudgetEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Aquí necesitarías obtener el nombre de la categoría usando budget.categoryId
                // Esto podría requerir una query adicional en el ViewModel o pasar el nombre de la categoría
                Text("Categoría ID: ${budget.categoryId}", style = MaterialTheme.typography.titleMedium) // Placeholder
                Text("Mes: ${budget.month}/${budget.year}", style = MaterialTheme.typography.bodySmall)
            }
            Text("$${String.format("%.2f", budget.amount)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

