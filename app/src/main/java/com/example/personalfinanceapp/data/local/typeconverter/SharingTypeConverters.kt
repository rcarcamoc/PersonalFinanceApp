package com.example.personalfinanceapp.data.local.typeconverter

import androidx.room.TypeConverter
import com.example.personalfinanceapp.domain.model.sharing.UserRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.personalfinanceapp.domain.model.sharing.SharedSourceInfo

/**
 * TypeConverters para la base de datos Room, específicamente para modelos de compartición.
 */
class SharingTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromUserRole(role: UserRole?): String? {
        return role?.name
    }

    @TypeConverter
    fun toUserRole(roleName: String?): UserRole? {
        return roleName?.let { UserRole.valueOf(it) }
    }

    // TypeConverter para List<SharedSourceInfo> (Ejemplo)
    // Esto es si decides almacenar una lista compleja como un String JSON en una entidad.
    // Si SharedSourceInfo se convierte en su propia tabla con relaciones, esto no sería necesario para esa lista específica.
    @TypeConverter
    fun fromSharedSourceInfoList(list: List<SharedSourceInfo>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toSharedSourceInfoList(json: String?): List<SharedSourceInfo>? {
        return json?.let {
            val type = object : TypeToken<List<SharedSourceInfo>>() {}.type
            gson.fromJson(json, type)
        }
    }

    // Puedes agregar más conversores según sea necesario para otros tipos complejos
    // que no son soportados directamente por Room y que quieras almacenar como Strings.
}

