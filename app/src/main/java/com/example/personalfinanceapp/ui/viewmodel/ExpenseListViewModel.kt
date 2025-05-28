package com.example.personalfinanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.domain.usecase.expense.GetExpensesUseCase
import com.example.personalfinanceapp.domain.usecase.expense.DeleteExpenseUseCase
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
 * Estado para la pantalla de lista de gastos.
 */
data class ExpenseListState(
    val isLoading: Boolean = false,
    val expenses: List<ExpenseEntity> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla que muestra la lista de gastos.
 *
 * @property getExpensesUseCase Caso de uso para obtener todos los gastos.
 * @property deleteExpenseUseCase Caso de uso para eliminar un gasto.
 */
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase // Para futuras funcionalidades de eliminar desde la lista
) : ViewModel() {

    private val _expenseListState = MutableStateFlow(ExpenseListState())
    val expenseListState: StateFlow<ExpenseListState> = _expenseListState.asStateFlow()

    init {
        loadExpenses()
    }

    /**
     * Carga la lista de gastos desde el repositorio.
     */
    fun loadExpenses() {
        viewModelScope.launch {
            getExpensesUseCase()
                .onEach { expenses ->
                    _expenseListState.value = ExpenseListState(expenses = expenses)
                }
                .catch { e ->
                    _expenseListState.value = ExpenseListState(error = e.message ?: "Error desconocido al cargar gastos")
                }
                .launchIn(viewModelScope)
        }
    }

    /**
     * Elimina un gasto.
     * (Esta función es un ejemplo, se podría llamar desde la UI al deslizar o mantener presionado un ítem)
     */
    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                deleteExpenseUseCase(expense)
                // La lista se actualizará automáticamente si el Flow de getExpensesUseCase está bien implementado
                // o se puede llamar a loadExpenses() explícitamente si es necesario.
            } catch (e: Exception) {
                _expenseListState.value = _expenseListState.value.copy(error = e.message ?: "Error al eliminar el gasto")
            }
        }
    }
}

