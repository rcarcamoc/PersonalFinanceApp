package com.example.personalfinanceapp.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.personalfinanceapp.data.local.CategoryDao
import com.example.personalfinanceapp.data.local.CategoryEntity
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
class CategoryRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryRepository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        categoryDao = mock()
        categoryRepository = CategoryRepositoryImpl(categoryDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCategories SHOULD return flow of categories from DAO`() = runTest {
        // Arrange
        val fakeCategories = listOf(
            CategoryEntity(1, "Comida"),
            CategoryEntity(2, "Transporte")
        )
        whenever(categoryDao.getAllCategories()).thenReturn(flowOf(fakeCategories))

        // Act
        val resultFlow = categoryRepository.getCategories()
        val resultList = resultFlow.first()

        // Assert
        assertEquals("La lista de categorías debe coincidir con la del DAO", fakeCategories, resultList)
        verify(categoryDao).getAllCategories()
    }

    @Test
    fun `insertCategory SHOULD call insertCategory on DAO`() = runTest {
        // Arrange
        val newCategory = CategoryEntity(3, "Ocio")

        // Act
        categoryRepository.insertCategory(newCategory)

        // Assert
        verify(categoryDao).insertCategory(newCategory)
    }

    @Test
    fun `getCategoryById SHOULD return correct category from DAO`() = runTest {
        // Arrange
        val categoryId = 1L
        val fakeCategory = CategoryEntity(categoryId, "Comida")
        whenever(categoryDao.getCategoryById(categoryId)).thenReturn(flowOf(fakeCategory))

        // Act
        val resultFlow = categoryRepository.getCategoryById(categoryId)
        val result = resultFlow.first()

        // Assert
        assertEquals(fakeCategory, result)
        verify(categoryDao).getCategoryById(categoryId)
    }

    @Test
    fun `deleteCategory SHOULD call deleteCategory on DAO`() = runTest {
        // Arrange
        val categoryToDelete = CategoryEntity(1, "Comida")

        // Act
        categoryRepository.deleteCategory(categoryToDelete)

        // Assert
        verify(categoryDao).deleteCategory(categoryToDelete)
    }
}

