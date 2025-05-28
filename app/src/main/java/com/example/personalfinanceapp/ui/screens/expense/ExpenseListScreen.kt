package com.example.personalfinanceapp.ui.screens.expense

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
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.ui.viewmodel.ExpenseListViewModel

/**
 * Pantalla que muestra una lista de gastos.
 * Permite ver los gastos y potencialmente agregar nuevos o editarlos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel = hiltViewModel(),
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (Long) -> Unit // Para navegar a la pantalla de detalle/edición del gasto
) {
    val state by viewModel.expenseListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Gastos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) {
                Icon(Icons.Filled.Add, "Agregar Gasto")
            }
        }
    ) {ecationPadding ->
        Column(
            modifier = Modifier
                .padding(recationPadding)
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
                if (state.expenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay gastos registrados.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.expenses) { expense ->
                            ExpenseItem(expense = expense, onClick = { onExpenseClick(expense.id) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable para mostrar un único ítem de gasto en la lista.
 */
@Composable
fun ExpenseItem(
    expense: ExpenseEntity,
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
                Text(expense.merchant, style = MaterialTheme.typography.titleMedium)
                Text("Fecha: ${expense.date} ${expense.time}", style = MaterialTheme.typography.bodySmall)
                expense.lastCardDigits?.let {
                    Text("Tarjeta: **** $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("$${String.format("%.2f", expense.amount)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

