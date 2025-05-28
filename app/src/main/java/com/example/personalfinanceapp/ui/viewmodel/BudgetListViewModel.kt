package com.example.personalfinanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.local.BudgetEntity
import com.example.personalfinanceapp.domain.usecase.budget.GetBudgetsUseCase
import com.example.personalfinanceapp.domain.usecase.budget.DeleteBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado para la pantalla de lista de presupuestos.
 */
data class BudgetListState(
    val isLoading: Boolean = false,
    val budgets: List<BudgetEntity> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla que muestra la lista de presupuestos.
 *
 * @property getBudgetsUseCase Caso de uso para obtener todos los presupuestos.
 * @property deleteBudgetUseCase Caso de uso para eliminar un presupuesto.
 */
@HiltViewModel
class BudgetListViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase // Para futuras funcionalidades de eliminar desde la lista
) : ViewModel() {

    private val _budgetListState = MutableStateFlow(BudgetListState())
    val budgetListState: StateFlow<BudgetListState> = _budgetListState.asStateFlow()

    init {
        loadBudgets()
    }

    /**
     * Carga la lista de presupuestos desde el repositorio.
     * Aquí podrías pasar parámetros como mes y año si la UI lo requiere.
     */
    fun loadBudgets(month: Int? = null, year: Int? = null) { // Ejemplo: permitir filtrar por mes/año
        viewModelScope.launch {
            // Lógica para decidir si obtener todos o filtrar
            // Por ahora, obtenemos todos como ejemplo
            getBudgetsUseCase() // Modificar para aceptar filtros si es necesario
                .onEach { budgets ->
                    _budgetListState.value = BudgetListState(budgets = budgets)
                }
                .catch { e ->
                    _budgetListState.value = BudgetListState(error = e.message ?: "Error desconocido al cargar presupuestos")
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * Elimina un presupuesto.
     */
    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            try {
                deleteBudgetUseCase(budget)
                // La lista se actualizará automáticamente
            } catch (e: Exception) {
                _budgetListState.value = _budgetListState.value.copy(error = e.message ?: "Error al eliminar el presupuesto")
            }
        }
    }
}

