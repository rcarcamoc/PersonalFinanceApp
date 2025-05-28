package com.example.personalfinanceapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidad que representa un gasto en la base de datos Room.
 *
 * @property id El identificador único del gasto (autogenerado).
 * @property amount El monto del gasto.
 * @property date La fecha en que se realizó el gasto (timestamp o String formateada).
 * @property time La hora en que se realizó el gasto (timestamp o String formateada).
 * @property merchant El nombre del comercio donde se realizó el gasto.
 * @property categoryId El ID de la categoría a la que pertenece el gasto (clave foránea a CategoryEntity).
 * @property installments El número de cuotas (si aplica).
 * @property lastCardDigits Los últimos 4 dígitos de la tarjeta utilizada.
 * @property description Una descripción adicional del gasto (opcional).
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL // O define otra acción como NO_ACTION o RESTRICT
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val date: String, // Considerar usar Long para timestamp para facilitar ordenamiento y queries
    val time: String, // Considerar usar Long para timestamp
    val merchant: String,
    val categoryId: Long?,
    val installments: Int? = null,
    val lastCardDigits: String? = null,
    val description: String? = null // Campo para notas o detalles adicionales
)

