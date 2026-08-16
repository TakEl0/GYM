/**
 * @file EntidadEjercicio.kt
 * @brief Entidad Room que representa un ejercicio del catálogo del usuario.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadEjercicio
 * @brief Ejercicio del catálogo con su grupo muscular principal y secundario,
 * la máquina asociada y el equipamiento necesario.
 */
@Entity(
    tableName = "ejercicio",
    indices = [Index(value = ["userId"])]
)
data class EntidadEjercicio(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val grupoMuscularPrincipal: String,
    val grupoMuscularSecundario: String? = null,
    val maquinaId: String? = null,
    val equipamiento: String, // MAQUINA_GUIADA / POLEA / BARRA / MANCUERNAS / CALISTENIA
    val instrucciones: String? = null
)