package com.example.personalfinanceapp.data.repository

import com.example.personalfinanceapp.data.local.ExpenseDao
import com.example.personalfinanceapp.data.local.ExpenseEntity
import com.example.personalfinanceapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de gastos.
 * Esta clase se encarga de obtener los datos de gastos desde el DAO local (Room).
 * En futuras implementaciones, podría coordinar datos de fuentes remotas (Gmail, Drive).
 */
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
    // Aquí se podrían inyectar DataSources para Gmail y Drive más adelante
    // private val gmailDataSource: GmailDataSource,
    // private val driveDataSource: DriveDataSource
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    override fun getExpenseById(id: Long): Flow<ExpenseEntity?> {
        return expenseDao.getExpenseById(id)
    }

    override suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    override fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategoryId(categoryId)
    }

    override fun getExpensesBetweenDates(startDate: String, endDate: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    // Ejemplo de cómo se podrían integrar las fuentes de datos remotas en el futuro:
    /*
    override suspend fun fetchExpensesFromGmail(query: String): Flow<List<ExpenseEntity>> {
        // Lógica para llamar a gmailDataSource, parsear correos y mapear a ExpenseEntity
        // Luego, posiblemente guardar en Room a través de expenseDao
        return flow { emit(emptyList()) } // Placeholder
    }

    override suspend fun syncExpensesWithDrive(): Flow<Boolean> {
        // Lógica para interactuar con driveDataSource, subir/bajar JSON y fusionar con Room
        return flow { emit(true) } // Placeholder
    }
    */
}

