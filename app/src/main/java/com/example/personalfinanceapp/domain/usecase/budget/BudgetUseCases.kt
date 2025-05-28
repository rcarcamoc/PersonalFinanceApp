package com.example.personalfinanceapp.domain.usecase.budget

import com.example.personalfinanceapp.data.local.BudgetEntity
import com.example.personalfinanceapp.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los presupuestos.
 */
class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(): Flow<List<BudgetEntity>> {
        return budgetRepository.getAllBudgets()
    }
}

/**
 * Caso de uso para obtener el presupuesto de una categoría en un mes y año específicos.
 */
class GetBudgetForCategoryAndMonthUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(categoryId: Long, month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetRepository.getBudgetForCategoryAndMonth(categoryId, month, year)
    }
}

/**
 * Caso de uso para obtener todos los presupuestos de un mes y año específicos.
 */
class GetBudgetsForMonthUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<List<BudgetEntity>> {
        return budgetRepository.getBudgetsForMonth(month, year)
    }
}

/**
 * Caso de uso para insertar un presupuesto.
 */
class InsertBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: BudgetEntity): Long {
        return budgetRepository.insertBudget(budget)
    }
}

/**
 * Caso de uso para actualizar un presupuesto.
 */
class UpdateBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: BudgetEntity) {
        budgetRepository.updateBudget(budget)
    }
}

/**
 * Caso de uso para eliminar un presupuesto.
 */
class DeleteBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: BudgetEntity) {
        budgetRepository.deleteBudget(budget)
    }
}

