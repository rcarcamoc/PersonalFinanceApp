package com.example.personalfinanceapp.domain.repository

import com.example.personalfinanceapp.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de categorías.
 * Define los métodos para interactuar con los datos de categorías.
 */
interface CategoryRepository {
    /**
     * Obtiene todas las categorías almacenadas localmente.
     */
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Obtiene una categoría específica por su ID.
     */
    fun getCategoryById(id: Long): Flow<CategoryEntity?>

    /**
     * Inserta una nueva categoría en el almacenamiento local.
     */
    suspend fun insertCategory(category: CategoryEntity): Long

    /**
     * Actualiza una categoría existente en el almacenamiento local.
     */
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * Elimina una categoría del almacenamiento local.
     */
    suspend fun deleteCategory(category: CategoryEntity)
}

