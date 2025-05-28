package com.example.personalfinanceapp.ui.screens.category

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
import com.example.personalfinanceapp.data.local.CategoryEntity
import com.example.personalfinanceapp.ui.viewmodel.CategoryListViewModel

/**
 * Pantalla que muestra una lista de categorías.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoryListViewModel = hiltViewModel(),
    onAddCategoryClick: () -> Unit,
    onCategoryClick: (Long) -> Unit // Para navegar a la pantalla de detalle/edición de la categoría
) {
    val state by viewModel.categoryListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategoryClick) {
                Icon(Icons.Filled.Add, "Agregar Categoría")
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
                if (state.categories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay categorías registradas.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.categories) { category ->
                            CategoryItem(category = category, onClick = { onCategoryClick(category.id) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable para mostrar un único ítem de categoría en la lista.
 */
@Composable
fun CategoryItem(
    category: CategoryEntity,
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
            Text(category.name, style = MaterialTheme.typography.titleMedium)
            // Podrías agregar un icono o más detalles si es necesario
        }
    }
}

