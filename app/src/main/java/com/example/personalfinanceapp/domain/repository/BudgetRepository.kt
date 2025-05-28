package com.example.personalfinanceapp.domain.repository

import com.example.personalfinanceapp.data.local.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de presupuestos.
 * Define los métodos para interactuar con los datos de presupuestos.
 */
interface BudgetRepository {
    /**
     * Obtiene todos los presupuestos almacenados localmente.
     */
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    /**
     * Obtiene el presupuesto para una categoría y mes específicos.
     */
    fun getBudgetForCategoryAndMonth(categoryId: Long, month: Int, year: Int): Flow<BudgetEntity?>

    /**
     * Obtiene todos los presupuestos para un mes y año específicos.
     */
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    /**
     * Inserta un nuevo presupuesto en el almacenamiento local.
     */
    suspend fun insertBudget(budget: BudgetEntity): Long

    /**
     * Actualiza un presupuesto existente en el almacenamiento local.
     */
    suspend fun updateBudget(budget: BudgetEntity)

    /**
     * Elimina un presupuesto del almacenamiento local.
     */
    suspend fun deleteBudget(budget: BudgetEntity)
}

