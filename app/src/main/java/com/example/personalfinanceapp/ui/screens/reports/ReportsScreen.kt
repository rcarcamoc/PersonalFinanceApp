package com.example.personalfinanceapp.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.ui.viewmodel.reports.BudgetComparisonData
import com.example.personalfinanceapp.ui.viewmodel.reports.ReportsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

/**
 * Pantalla principal de Reportes.
 * Muestra varias visualizaciones y resúmenes de los datos financieros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.reportsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes Financieros") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        state.error?.let {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
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
                SummarySection(state.totalExpenses, state.totalBudget)
            }

            if (state.alerts.isNotEmpty()) {
                item {
                    AlertsSection(alerts = state.alerts)
                }
            }

            if (state.insights.isNotEmpty()) {
                item {
                    InsightsSection(insights = state.insights)
                }
            }

            if (state.expensesPerCategory.isNotEmpty()) {
                item {
                    ExpensesPerCategoryChart(data = state.expensesPerCategory)
                }
            }

            if (state.budgetVsActual.isNotEmpty()) {
                item {
                    BudgetVsActualChart(data = state.budgetVsActual)
                }
            }

            if (state.recentExpenses.isNotEmpty()) {
                item {
                    RecentExpensesSection(expenses = state.recentExpenses)
                }
            }
        }
    }
}

@Composable
fun SummarySection(totalExpenses: Double, totalBudget: Double) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Resumen del Mes Actual", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Gastado: $${String.format("%.2f", totalExpenses)}")
            Text("Total Presupuestado: $${String.format("%.2f", totalBudget)}")
            val difference = totalBudget - totalExpenses
            Text(
                "Diferencia: $${String.format("%.2f", difference)}",
                color = if (difference >= 0) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AlertsSection(alerts: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Alertas Importantes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            alerts.forEach {
                Text("• $it", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun InsightsSection(insights: List<String>) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Insights Automáticos", style = MaterialTheme.typography.titleMedium)
            insights.forEach {
                Text("• $it", modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun ExpensesPerCategoryChart(data: Map<String, Double>) {
    val context = LocalContext.current
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Gastos por Categoría (Mes Actual)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            AndroidView(
                factory = { PieChart(context) },
                modifier = Modifier.fillMaxWidth().height(300.dp),
                update = {
                    val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 12f
                    }
                    it.data = PieData(dataSet)
                    it.description.isEnabled = false
                    it.isDrawHoleEnabled = true
                    it.setEntryLabelColor(android.graphics.Color.BLACK)
                    it.invalidate()
                }
            )
        }
    }
}

@Composable
fun BudgetVsActualChart(data: List<BudgetComparisonData>) {
    val context = LocalContext.current
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Presupuesto vs. Gasto Real (Mes Actual)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            AndroidView(
                factory = { BarChart(context) },
                modifier = Modifier.fillMaxWidth().height(300.dp),
                update = {
                    val budgetedEntries = data.mapIndexed { index, item -> BarEntry(index.toFloat(), item.budgetedAmount.toFloat()) }
                    val actualEntries = data.mapIndexed { index, item -> BarEntry(index.toFloat(), item.actualAmount.toFloat()) }

                    val budgetedDataSet = BarDataSet(budgetedEntries, "Presupuestado").apply {
                        color = ColorTemplate.rgb("#AED581") // Verde claro
                        valueTextSize = 10f
                    }
                    val actualDataSet = BarDataSet(actualEntries, "Real").apply {
                        color = ColorTemplate.rgb("#FF8A65") // Naranja claro
                        valueTextSize = 10f
                    }

                    val barData = BarData(budgetedDataSet, actualDataSet)
                    val groupSpace = 0.3f
                    val barSpace = 0.05f
                    val barWidth = 0.3f // (barSpace + barWidth) * numGroups + groupSpace = 1.0
                    barData.barWidth = barWidth

                    it.data = barData
                    it.groupBars(0f, groupSpace, barSpace)
                    it.description.isEnabled = false
                    it.xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(data.map { item -> item.categoryName })
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        isGranularityEnabled = true
                        setDrawGridLines(false)
                        labelRotationAngle = -45f
                    }
                    it.axisLeft.axisMinimum = 0f
                    it.axisRight.isEnabled = false
                    it.invalidate()
                }
            )
        }
    }
}

@Composable
fun RecentExpensesSection(expenses: List<ExpenseEntity>) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Gastos Recientes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (expenses.isEmpty()) {
                Text("No hay gastos recientes.")
            } else {
                expenses.forEach {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(it.merchant, modifier = Modifier.weight(1f))
                        Text("$${String.format("%.2f", it.amount)}")
                    }
                    Divider()
                }
            }
        }
    }
}


