package com.example.personalfinanceapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.domain.usecase.expense.DeleteExpenseUseCase
import com.example.personalfinanceapp.domain.usecase.expense.GetExpensesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ExpenseListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getExpensesUseCase: GetExpensesUseCase
    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase
    private lateinit var expenseListViewModel: ExpenseListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getExpensesUseCase = mock()
        deleteExpenseUseCase = mock()
        // ViewModel se inicializa aquí para que loadExpenses() en init {} se llame con los mocks listos
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadExpenses SHOULD update state with expenses on successful fetch`() = runTest {
        // Arrange
        val fakeExpenses = listOf(
            ExpenseEntity(1, 100.0, "2023-01-01", "10:00", "Tienda A", 1, null, "1234", "Compra A"),
            ExpenseEntity(2, 50.0, "2023-01-02", "11:00", "Tienda B", 2, null, "5678", "Compra B")
        )
        whenever(getExpensesUseCase.invoke()).thenReturn(flowOf(fakeExpenses))
        expenseListViewModel = ExpenseListViewModel(getExpensesUseCase, deleteExpenseUseCase) // Inicializar después de whenever

        // Act
        // loadExpenses() es llamado en el init del ViewModel, así que el estado ya debería estar actualizado.
        // Para forzar una recarga o probar la función explícitamente:
        // expenseListViewModel.loadExpenses()
        advanceUntilIdle() // Asegurar que las corrutinas del init se completen

        // Assert
        val state = expenseListViewModel.expenseListState.value
        assertFalse("isLoading should be false after loading", state.isLoading)
        assertEquals("Expenses list should match fake data", fakeExpenses, state.expenses)
        assertNull("Error should be null on success", state.error)
        verify(getExpensesUseCase).invoke()
    }

    @Test
    fun `loadExpenses SHOULD update state with error on failed fetch`() = runTest {
        // Arrange
        val errorMessage = "Error al cargar gastos"
        whenever(getExpensesUseCase.invoke()).thenReturn(flowOf { throw Exception(errorMessage) })
        expenseListViewModel = ExpenseListViewModel(getExpensesUseCase, deleteExpenseUseCase)

        // Act
        advanceUntilIdle()

        // Assert
        val state = expenseListViewModel.expenseListState.value
        assertFalse("isLoading should be false even on error", state.isLoading) // Depende de la implementación, podría ser true si el error es antes de setear loading a false
        assertTrue("Expenses list should be empty on error", state.expenses.isEmpty())
        assertEquals("Error message should match", errorMessage, state.error)
        verify(getExpensesUseCase).invoke()
    }

    @Test
    fun `deleteExpense SHOULD call deleteExpenseUseCase and refresh list (implicitly)`() = runTest {
        // Arrange
        val expenseToDelete = ExpenseEntity(1, 100.0, "2023-01-01", "10:00", "Tienda A", 1, null, "1234", "Compra A")
        val initialExpenses = listOf(expenseToDelete, ExpenseEntity(2, 50.0, "2023-01-02", "11:00", "Tienda B", 2, null, "5678", "Compra B"))
        val expensesAfterDelete = listOf(ExpenseEntity(2, 50.0, "2023-01-02", "11:00", "Tienda B", 2, null, "5678", "Compra B"))

        // Simular que getExpensesUseCase emite la lista inicial y luego la actualizada después de la eliminación
        whenever(getExpensesUseCase.invoke())
            .thenReturn(flowOf(initialExpenses)) // Emisión inicial
            // Si la eliminación refresca la lista, el Flow de getExpensesUseCase debería emitir de nuevo.
            // Esto es más complejo de mockear directamente sin un StateFlow real en el repositorio.
            // Una forma más simple es verificar que deleteUseCase fue llamado y que loadExpenses (o el Flow) se actualiza.

        expenseListViewModel = ExpenseListViewModel(getExpensesUseCase, deleteExpenseUseCase)
        advanceUntilIdle() // Carga inicial

        // Act
        expenseListViewModel.deleteExpense(expenseToDelete)
        advanceUntilIdle() // Procesar la eliminación

        // Assert
        verify(deleteExpenseUseCase).invoke(expenseToDelete)
        // Para verificar la actualización de la lista, necesitarías que el mock de getExpensesUseCase
        // se comporte de manera que refleje el cambio, o que loadExpenses() se llame explícitamente
        // y se mockee su comportamiento de nuevo. Si el Flow se actualiza automáticamente, el estado debería reflejarlo.
        // Por simplicidad, aquí solo verificamos la llamada al use case.
        // Si el Flow de getExpensesUseCase es un StateFlow en el repo que se actualiza, el test sería más robusto.
    }

    @Test
    fun `deleteExpense SHOULD update state with error on failed deletion`() = runTest {
        // Arrange
        val expenseToDelete = ExpenseEntity(1, 100.0, "2023-01-01", "10:00", "Tienda A", 1, null, "1234", "Compra A")
        val errorMessage = "Error al eliminar"
        whenever(getExpensesUseCase.invoke()).thenReturn(flowOf(listOf(expenseToDelete))) // Estado inicial
        whenever(deleteExpenseUseCase.invoke(expenseToDelete)).thenThrow(RuntimeException(errorMessage))
        expenseListViewModel = ExpenseListViewModel(getExpensesUseCase, deleteExpenseUseCase)
        advanceUntilIdle()

        // Act
        expenseListViewModel.deleteExpense(expenseToDelete)
        advanceUntilIdle()

        // Assert
        val state = expenseListViewModel.expenseListState.value
        assertEquals("Error message should be set on failed deletion", errorMessage, state.error)
        verify(deleteExpenseUseCase).invoke(expenseToDelete)
    }
}

