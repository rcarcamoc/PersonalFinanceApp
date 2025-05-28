package com.example.personalfinanceapp.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule // Para LiveData si se usa, o para ViewModels
import com.example.personalfinanceapp.data.local.ExpenseDao
import com.example.personalfinanceapp.data.local.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.* // Importar todos los asserts de JUnit
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias para [ExpenseRepositoryImpl].
 *
 * Estas pruebas verifican la interacción del repositorio con su DAO y la correcta
 * transformación de datos.
 * Se utiliza Mockito para simular el comportamiento del DAO.
 * Es necesario agregar la dependencia de Mockito-Kotlin al build.gradle (testImplementation "org.mockito.kotlin:mockito-kotlin:x.y.z")
 * y configurar el entorno de pruebas para corrutinas.
 */
@ExperimentalCoroutinesApi
class ExpenseRepositoryImplTest {

    // Regla para ejecutar tareas de LiveData/ViewModel de forma síncrona (útil para ViewModels)
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Dispatcher para controlar la ejecución de corrutinas en las pruebas
    private val testDispatcher = StandardTestDispatcher()

    // Mock del DAO de gastos
    private lateinit var expenseDao: ExpenseDao

    // Instancia del repositorio a probar
    private lateinit var expenseRepository: ExpenseRepositoryImpl

    @Before
    fun setUp() {
        // Configurar el dispatcher principal para las pruebas de corrutinas
        Dispatchers.setMain(testDispatcher)
        // Crear el mock del DAO
        expenseDao = mock()
        // Inicializar el repositorio con el DAO mockeado
        expenseRepository = ExpenseRepositoryImpl(expenseDao)
    }

    @After
    fun tearDown() {
        // Resetear el dispatcher principal después de cada prueba
        Dispatchers.resetMain()
    }

    @Test
    fun `getExpenses SHOULD return flow of expenses from DAO`() = runTest {
        // Arrange: Preparar los datos de prueba y el comportamiento del mock
        val fakeExpenses = listOf(
            ExpenseEntity(1, 100.0, "2023-01-01", "10:00", "Tienda A", 1, null, "1234", "Compra A"),
            ExpenseEntity(2, 50.0, "2023-01-02", "11:00", "Tienda B", 2, null, "5678", "Compra B")
        )
        whenever(expenseDao.getAllExpenses()).thenReturn(flowOf(fakeExpenses))

        // Act: Ejecutar la función a probar
        val resultFlow = expenseRepository.getExpenses()
        val resultList = resultFlow.first() // Recolectar el primer valor emitido por el Flow

        // Assert: Verificar que el resultado es el esperado
        assertEquals("La lista de gastos debe coincidir con la del DAO", fakeExpenses, resultList)
        verify(expenseDao).getAllExpenses() // Verificar que se llamó al método correcto del DAO
    }

    @Test
    fun `insertExpense SHOULD call insertExpense on DAO`() = runTest {
        // Arrange
        val newExpense = ExpenseEntity(3, 75.0, "2023-01-03", "12:00", "Tienda C", 1, null, "9012", "Compra C")

        // Act
        expenseRepository.insertExpense(newExpense)

        // Assert
        verify(expenseDao).insertExpense(newExpense) // Verificar que el DAO fue llamado con el gasto correcto
    }

    @Test
    fun `getExpenseById SHOULD return correct expense from DAO`() = runTest {
        // Arrange
        val expenseId = 1L
        val fakeExpense = ExpenseEntity(expenseId, 100.0, "2023-01-01", "10:00", "Tienda A", 1, null, "1234", "Compra A")
        whenever(expenseDao.getExpenseById(expenseId)).thenReturn(flowOf(fakeExpense))

        // Act
        val resultFlow = expenseRepository.getExpenseById(expenseId)
        val result = resultFlow.first()

        // Assert
        assertEquals(fakeExpense, result)
        verify(expenseDao).getExpenseById(expenseId)
    }
    
    @Test
    fun `deleteExpense SHOULD call deleteExpense on DAO`() = runTest {
        // Arrange
        val expenseToDelete = ExpenseEntity(1, 100.0, "2023-01-01", "10:00", "Tienda A", 1, null, "1234", "Compra A")
        
        // Act
        expenseRepository.deleteExpense(expenseToDelete)
        
        // Assert
        verify(expenseDao).deleteExpense(expenseToDelete)
    }

    // Aquí se podrían agregar más pruebas para updateExpense, getExpensesByFilters, etc.
    // Por ejemplo, para probar filtros, se necesitaría simular el comportamiento del DAO
    // con diferentes parámetros de query.
}

