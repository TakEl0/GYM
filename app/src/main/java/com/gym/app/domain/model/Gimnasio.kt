/**
 * @file Gimnasio.kt
 * @brief Modelo de dominio que representa el gimnasio donde entrena el usuario.
 *
 * El gimnasio actúa como catálogo de máquinas disponibles, permitiendo seleccionar
 * los equipamientos reales del centro a la hora de configurar o sustituir ejercicios
 * de las rutinas de entrenamiento.
 */
package com.gym.app.domain.model

/**
 * @class Gimnasio
 * @brief Representa un gimnasio con su información básica y su parque de máquinas.
 *
 * @property id Identificador único del gimnasio.
 * @property nombre Nombre comercial del gimnasio.
 * @property direccion Dirección física del gimnasio (opcional).
 * @property maquinas Lista de máquinas y equipamientos disponibles en el gimnasio.
 */
data class Gimnasio(
    val id: String,
    val nombre: String,
    val direccion: String? = null,
    val maquinas: List<Maquina> = emptyList()
)