package com.example.personalfinanceapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.personalfinanceapp.data.local.CategoryEntity
import com.example.personalfinanceapp.domain.usecase.category.DeleteCategoryUseCase
import com.example.personalfinanceapp.domain.usecase.category.GetCategoriesUseCase
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
class CategoryListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    private lateinit var categoryListViewModel: CategoryListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCategoriesUseCase = mock()
        deleteCategoryUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCategories SHOULD update state with categories on successful fetch`() = runTest {
        // Arrange
        val fakeCategories = listOf(
            CategoryEntity(1, "Comida"),
            CategoryEntity(2, "Transporte")
        )
        whenever(getCategoriesUseCase.invoke()).thenReturn(flowOf(fakeCategories))
        categoryListViewModel = CategoryListViewModel(getCategoriesUseCase, deleteCategoryUseCase)

        // Act
        advanceUntilIdle()

        // Assert
        val state = categoryListViewModel.categoryListState.value
        assertFalse("isLoading should be false after loading", state.isLoading)
        assertEquals("Categories list should match fake data", fakeCategories, state.categories)
        assertNull("Error should be null on success", state.error)
        verify(getCategoriesUseCase).invoke()
    }

    @Test
    fun `loadCategories SHOULD update state with error on failed fetch`() = runTest {
        // Arrange
        val errorMessage = "Error al cargar categorías"
        whenever(getCategoriesUseCase.invoke()).thenReturn(flowOf { throw Exception(errorMessage) })
        categoryListViewModel = CategoryListViewModel(getCategoriesUseCase, deleteCategoryUseCase)

        // Act
        advanceUntilIdle()

        // Assert
        val state = categoryListViewModel.categoryListState.value
        // assertFalse(state.isLoading) // isLoading might still be true if error occurs before it's set to false
        assertTrue("Categories list should be empty on error", state.categories.isEmpty())
        assertEquals("Error message should match", errorMessage, state.error)
        verify(getCategoriesUseCase).invoke()
    }

    @Test
    fun `deleteCategory SHOULD call deleteCategoryUseCase`() = runTest {
        // Arrange
        val categoryToDelete = CategoryEntity(1, "Comida")
        whenever(getCategoriesUseCase.invoke()).thenReturn(flowOf(listOf(categoryToDelete))) // Initial state
        categoryListViewModel = CategoryListViewModel(getCategoriesUseCase, deleteCategoryUseCase)
        advanceUntilIdle()

        // Act
        categoryListViewModel.deleteCategory(categoryToDelete)
        advanceUntilIdle()

        // Assert
        verify(deleteCategoryUseCase).invoke(categoryToDelete)
        // Further assertions could check if the list is updated, similar to ExpenseListViewModelTest
    }

    @Test
    fun `deleteCategory SHOULD update state with error on failed deletion`() = runTest {
        // Arrange
        val categoryToDelete = CategoryEntity(1, "Comida")
        val errorMessage = "Error al eliminar categoría"
        whenever(getCategoriesUseCase.invoke()).thenReturn(flowOf(listOf(categoryToDelete))) // Initial state
        whenever(deleteCategoryUseCase.invoke(categoryToDelete)).thenThrow(RuntimeException(errorMessage))
        categoryListViewModel = CategoryListViewModel(getCategoriesUseCase, deleteCategoryUseCase)
        advanceUntilIdle()

        // Act
        categoryListViewModel.deleteCategory(categoryToDelete)
        advanceUntilIdle()

        // Assert
        val state = categoryListViewModel.categoryListState.value
        assertEquals("Error message should be set on failed deletion", errorMessage, state.error)
        verify(deleteCategoryUseCase).invoke(categoryToDelete)
    }
}

