package com.example.personalfinanceapp.data.repository

import com.example.personalfinanceapp.data.local.BudgetDao
import com.example.personalfinanceapp.data.local.BudgetEntity
import com.example.personalfinanceapp.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de presupuestos.
 * Se encarga de obtener los datos de presupuestos desde el DAO local (Room).
 */
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getAllBudgets()
    }

    override fun getBudgetForCategoryAndMonth(categoryId: Long, month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetDao.getBudgetForCategoryAndMonth(categoryId, month, year)
    }

    override fun getBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonth(month, year)
    }

    override suspend fun insertBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }

    override suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    override suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }
}

