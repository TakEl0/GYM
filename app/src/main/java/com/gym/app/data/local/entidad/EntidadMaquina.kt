/**
 * @file EntidadMaquina.kt
 * @brief Entidad Room que representa una máquina o equipamiento del gimnasio.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadMaquina
 * @brief Máquina del gimnasio con los grupos musculares que trabaja y su tipo
 * de equipamiento (máquina guiada, polea, barra o mancuernas), marca, modelo,
 * ejercicios posibles y sinónimos serializados.
 */
@Entity(
    tableName = "maquina",
    indices = [Index(value = ["gimnasioId"])]
)
data class EntidadMaquina(
    @PrimaryKey
    val id: String,
    val gimnasioId: String,
    val nombre: String,
    val grupoMuscular: String, // join por coma
    val tipoEquipamiento: String, // MAQUINA_GUIADA / POLEA / BARRA / MANCUERNAS
    val disponible: Boolean,
    val marca: String? = null,
    val modelo: String? = null,
    val ejerciciosPosibles: String = "", // separados por comas
    val sinonimos: String = "" // separados por comas
)
