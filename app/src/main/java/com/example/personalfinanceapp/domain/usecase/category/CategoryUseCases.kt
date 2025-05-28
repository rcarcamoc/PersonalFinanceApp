package com.example.personalfinanceapp.domain.usecase.category

import com.example.personalfinanceapp.data.local.CategoryEntity
import com.example.personalfinanceapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las categorías.
 */
class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategoryEntity>> {
        return categoryRepository.getAllCategories()
    }
}

/**
 * Caso de uso para obtener una categoría por su ID.
 */
class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(id: Long): Flow<CategoryEntity?> {
        return categoryRepository.getCategoryById(id)
    }
}

/**
 * Caso de uso para insertar una categoría.
 */
class InsertCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: CategoryEntity): Long {
        return categoryRepository.insertCategory(category)
    }
}

/**
 * Caso de uso para actualizar una categoría.
 */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: CategoryEntity) {
        categoryRepository.updateCategory(category)
    }
}

/**
 * Caso de uso para eliminar una categoría.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: CategoryEntity) {
        categoryRepository.deleteCategory(category)
    }
}

