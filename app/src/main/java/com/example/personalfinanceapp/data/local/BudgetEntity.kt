package com.example.personalfinanceapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidad que representa un presupuesto asignado a una categoría en la base de datos Room.
 *
 * @property id El identificador único del presupuesto (autogenerado).
 * @property categoryId El ID de la categoría a la que se asigna este presupuesto (clave foránea a CategoryEntity).
 * @property amount El monto del presupuesto asignado.
 * @property month El mes para el cual se define el presupuesto (ej. "2023-10" para Octubre 2023).
 * @property year El año para el cual se define el presupuesto.
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Si se elimina una categoría, se eliminan sus presupuestos asociados
        )
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val amount: Double,
    val month: Int, // Representa el mes (1-12)
    val year: Int    // Representa el año (ej. 2023)
)

