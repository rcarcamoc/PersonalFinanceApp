package com.example.personalfinanceapp.domain.repository

import com.example.personalfinanceapp.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de gastos.
 * Define los métodos para interactuar con los datos de gastos, ya sea localmente (Room)
 * o remotamente (ej. sincronización con Drive o ingesta desde Gmail).
 */
interface ExpenseRepository {

    /**
     * Obtiene todos los gastos almacenados localmente.
     */
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    /**
     * Obtiene un gasto específico por su ID.
     */
    fun getExpenseById(id: Long): Flow<ExpenseEntity?>

    /**
     * Inserta un nuevo gasto en el almacenamiento local.
     */
    suspend fun insertExpense(expense: ExpenseEntity): Long

    /**
     * Actualiza un gasto existente en el almacenamiento local.
     */
    suspend fun updateExpense(expense: ExpenseEntity)

    /**
     * Elimina un gasto del almacenamiento local.
     */
    suspend fun deleteExpense(expense: ExpenseEntity)

    /**
     * Obtiene todos los gastos asociados a una categoría específica.
     */
    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseEntity>>

    /**
     * Obtiene los gastos dentro de un rango de fechas específico.
     */
    fun getExpensesBetweenDates(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    // Aquí se podrían agregar métodos para la ingesta desde Gmail o sincronización con Drive
    // suspend fun fetchExpensesFromGmail(query: String): Flow<List<ExpenseEntity>>
    // suspend fun syncExpensesWithDrive(): Flow<Boolean>
}

