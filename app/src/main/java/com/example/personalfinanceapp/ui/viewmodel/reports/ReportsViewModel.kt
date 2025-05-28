package com.example.personalfinanceapp.ui.viewmodel.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.local.BudgetEntity
import com.example.personalfinanceapp.data.local.CategoryEntity
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.domain.usecase.budget.GetBudgetsUseCase
import com.example.personalfinanceapp.domain.usecase.category.GetCategoriesUseCase
import com.example.personalfinanceapp.domain.usecase.expense.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Estado para la pantalla de reportes.
 */
data class ReportsScreenState(
    val isLoading: Boolean = true,
    val expensesPerCategory: Map<String, Double> = emptyMap(), // Nombre de categoría a total gastado
    val budgetVsActual: List<BudgetComparisonData> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val totalExpenses: Double = 0.0,
    val totalBudget: Double = 0.0,
    val insights: List<String> = emptyList(),
    val alerts: List<String> = emptyList(),
    val error: String? = null
)

data class BudgetComparisonData(
    val categoryName: String,
    val budgetedAmount: Double,
    val actualAmount: Double,
    val difference: Double,
    val isOverBudget: Boolean
)

/**
 * ViewModel para la pantalla de reportes.
 * Se encarga de obtener y procesar los datos para generar reportes y visualizaciones.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBudgetsUseCase: GetBudgetsUseCase
) : ViewModel() {

    private val _reportsState = MutableStateFlow(ReportsScreenState())
    val reportsState: StateFlow<ReportsScreenState> = _reportsState.asStateFlow()

    init {
        loadReportData()
    }

    fun loadReportData() {
        viewModelScope.launch {
            _reportsState.value = ReportsScreenState(isLoading = true)
            try {
                // Combinar los flows de gastos, categorías y presupuestos
                combine(
                    getExpensesUseCase(),
                    getCategoriesUseCase(),
                    getBudgetsUseCase() // Podrías filtrar por mes actual aquí
                ) { expenses, categories, budgets ->
                    processReportData(expenses, categories, budgets)
                }.collect { newState ->
                    _reportsState.value = newState
                }
            } catch (e: Exception) {
                _reportsState.value = ReportsScreenState(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    private fun processReportData(
        expenses: List<ExpenseEntity>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetEntity>
    ): ReportsScreenState {
        val categoryMap = categories.associateBy { it.id }
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-indexado
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // 1. Gastos por categoría (para el mes actual, como ejemplo)
        val expensesThisMonth = expenses.filter { expense ->
            try {
                val expenseDateParts = expense.date.split("-") // Asume formato YYYY-MM-DD
                expenseDateParts.size == 3 && expenseDateParts[0].toInt() == currentYear && expenseDateParts[1].toInt() == currentMonth
            } catch (e: Exception) { false }
        }

        val expensesPerCategory = expensesThisMonth
            .groupBy { it.categoryId }
            .mapKeys { categoryMap[it.key]?.name ?: "Sin Categoría" }
            .mapValues { it.value.sumOf { expense -> expense.amount } }

        // 2. Comparación Gasto vs Presupuesto por categoría (para el mes actual)
        val budgetsThisMonth = budgets.filter { it.month == currentMonth && it.year == currentYear }
        val budgetComparisonList = mutableListOf<BudgetComparisonData>()
        var totalActualSpendingThisMonth = 0.0
        var totalBudgetedThisMonth = 0.0

        categories.forEach { category ->
            val actualSpending = expensesPerCategory[category.name] ?: 0.0
            val budgetedAmount = budgetsThisMonth.find { it.categoryId == category.id }?.amount ?: 0.0
            totalActualSpendingThisMonth += actualSpending
            totalBudgetedThisMonth += budgetedAmount
            if (budgetedAmount > 0 || actualSpending > 0) { // Solo mostrar si hay presupuesto o gasto
                budgetComparisonList.add(
                    BudgetComparisonData(
                        categoryName = category.name,
                        budgetedAmount = budgetedAmount,
                        actualAmount = actualSpending,
                        difference = budgetedAmount - actualSpending,
                        isOverBudget = actualSpending > budgetedAmount
                    )
                )
            }
        }

        // 3. Listado filtrable de gastos recientes (ej. últimos 10)
        val recentExpenses = expenses.sortedByDescending { it.date + it.time }.take(10)

        // 4. Alertas visuales y Insights
        val alerts = mutableListOf<String>()
        val insights = mutableListOf<String>()

        budgetComparisonList.filter { it.isOverBudget && it.budgetedAmount > 0 }.forEach { comparison ->
            alerts.add("Alerta: Superaste el presupuesto de '${comparison.categoryName}' en $${String.format("%.2f", comparison.actualAmount - comparison.budgetedAmount)}.")
        }

        if (totalActualSpendingThisMonth > totalBudgetedThisMonth && totalBudgetedThisMonth > 0) {
            val overspendPercentage = ((totalActualSpendingThisMonth - totalBudgetedThisMonth) / totalBudgetedThisMonth) * 100
            insights.add("Gastaste un ${String.format("%.0f", overspendPercentage)}%% más de tu presupuesto total este mes.")
        } else if (totalBudgetedThisMonth > 0) {
            val savedPercentage = ((totalBudgetedThisMonth - totalActualSpendingThisMonth) / totalBudgetedThisMonth) * 100
            if (savedPercentage > 10) {
                 insights.add("¡Buen trabajo! Ahorraste un ${String.format("%.0f", savedPercentage)}%% de tu presupuesto total este mes.")
            }
        }
        if (expensesPerCategory.isNotEmpty()){
            val mostSpentCategory = expensesPerCategory.maxByOrNull { it.value }
            mostSpentCategory?.let {
                insights.add("Tu mayor gasto este mes fue en '${it.key}' con un total de $${String.format("%.2f", it.value)}.")
            }
        }

        return ReportsScreenState(
            isLoading = false,
            expensesPerCategory = expensesPerCategory,
            budgetVsActual = budgetComparisonList,
            recentExpenses = recentExpenses,
            totalExpenses = totalActualSpendingThisMonth,
            totalBudget = totalBudgetedThisMonth,
            insights = insights,
            alerts = alerts,
            error = null
        )
    }
}

