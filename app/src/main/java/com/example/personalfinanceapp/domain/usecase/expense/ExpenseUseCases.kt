package com.example.personalfinanceapp.domain.usecase.expense

import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los gastos.
 */
class GetExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<ExpenseEntity>> {
        return expenseRepository.getAllExpenses()
    }
}

/**
 * Caso de uso para obtener un gasto por su ID.
 */
class GetExpenseByIdUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(id: Long): Flow<ExpenseEntity?> {
        return expenseRepository.getExpenseById(id)
    }
}

/**
 * Caso de uso para insertar un gasto.
 */
class InsertExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseEntity): Long {
        return expenseRepository.insertExpense(expense)
    }
}

/**
 * Caso de uso para actualizar un gasto.
 */
class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseEntity) {
        expenseRepository.updateExpense(expense)
    }
}

/**
 * Caso de uso para eliminar un gasto.
 */
class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseEntity) {
        expenseRepository.deleteExpense(expense)
    }
}

/**
 * Caso de uso para obtener gastos por categoría.
 */
class GetExpensesByCategoryUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(categoryId: Long): Flow<List<ExpenseEntity>> {
        return expenseRepository.getExpensesByCategory(categoryId)
    }
}

/**
 * Caso de uso para obtener gastos entre fechas.
 */
class GetExpensesBetweenDatesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(startDate: String, endDate: String): Flow<List<ExpenseEntity>> {
        return expenseRepository.getExpensesBetweenDates(startDate, endDate)
    }
}

