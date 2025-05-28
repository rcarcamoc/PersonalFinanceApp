package com.example.personalfinanceapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una categoría de gasto en la base de datos Room.
 *
 * @property id El identificador único de la categoría (autogenerado).
 * @property name El nombre de la categoría (ej. "Comida", "Transporte", "Ocio").
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

