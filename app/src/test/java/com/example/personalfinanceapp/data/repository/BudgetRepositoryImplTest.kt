package com.example.personalfinanceapp.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.personalfinanceapp.data.local.BudgetDao
import com.example.personalfinanceapp.data.local.BudgetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class BudgetRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var budgetDao: BudgetDao
    private lateinit var budgetRepository: BudgetRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        budgetDao = mock()
        budgetRepository = BudgetRepositoryImpl(budgetDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getBudgets SHOULD return flow of budgets from DAO`() = runTest {
        // Arrange
        val fakeBudgets = listOf(
            BudgetEntity(1, 1L, 500.0, 1, 2023),
            BudgetEntity(2, 2L, 300.0, 1, 2023)
        )
        whenever(budgetDao.getAllBudgets()).thenReturn(flowOf(fakeBudgets))

        // Act
        val resultFlow = budgetRepository.getBudgets()
        val resultList = resultFlow.first()

        // Assert
        assertEquals("La lista de presupuestos debe coincidir con la del DAO", fakeBudgets, resultList)
        verify(budgetDao).getAllBudgets()
    }

    @Test
    fun `insertBudget SHOULD call insertBudget on DAO`() = runTest {
        // Arrange
        val newBudget = BudgetEntity(3, 1L, 200.0, 2, 2023)

        // Act
        budgetRepository.insertBudget(newBudget)

        // Assert
        verify(budgetDao).insertBudget(newBudget)
    }

    @Test
    fun `getBudgetById SHOULD return correct budget from DAO`() = runTest {
        // Arrange
        val budgetId = 1L
        val fakeBudget = BudgetEntity(budgetId, 1L, 500.0, 1, 2023)
        whenever(budgetDao.getBudgetById(budgetId)).thenReturn(flowOf(fakeBudget))

        // Act
        val resultFlow = budgetRepository.getBudgetById(budgetId)
        val result = resultFlow.first()

        // Assert
        assertEquals(fakeBudget, result)
        verify(budgetDao).getBudgetById(budgetId)
    }

    @Test
    fun `deleteBudget SHOULD call deleteBudget on DAO`() = runTest {
        // Arrange
        val budgetToDelete = BudgetEntity(1, 1L, 500.0, 1, 2023)

        // Act
        budgetRepository.deleteBudget(budgetToDelete)

        // Assert
        verify(budgetDao).deleteBudget(budgetToDelete)
    }

    @Test
    fun `getBudgetsByMonthAndYear SHOULD return filtered budgets from DAO`() = runTest {
        // Arrange
        val month = 1
        val year = 2023
        val fakeBudgets = listOf(BudgetEntity(1, 1L, 500.0, month, year))
        whenever(budgetDao.getBudgetsByMonthAndYear(month, year)).thenReturn(flowOf(fakeBudgets))

        // Act
        val resultFlow = budgetRepository.getBudgetsByMonthAndYear(month, year)
        val resultList = resultFlow.first()

        // Assert
        assertEquals("La lista de presupuestos filtrados debe coincidir con la del DAO", fakeBudgets, resultList)
        verify(budgetDao).getBudgetsByMonthAndYear(month, year)
    }
}

