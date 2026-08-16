/**
 * @file Converters.kt
 * @brief Convertidores de tipos para Room (conversión de listas a cadenas y viceversa).
 */
package com.gym.app.data.local.entidad

import androidx.room.TypeConverter

/**
 * @class Converters
 * @brief Proporciona métodos de conversión para tipos complejos en Room.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }
}
