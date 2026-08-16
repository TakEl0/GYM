/**
 * @file Comida.kt
 * @brief Modelo de dominio que representa una ingesta nutricional (comida).
 */
package com.gym.app.domain.model

import java.time.LocalDate

/**
 * @class Comida
 * @brief Representa una comida o ingesta registrada por el usuario (desayuno, comida, merienda, cena).
 * @property id Identificador único de la comida.
 * @property nombre Nombre descriptivo del plato o alimento.
 * @property kcal Calorías totales aportadas.
 * @property proteinasG Gramos de proteína.
 * @property carbohidratosG Gramos de carbohidratos.
 * @property grasasG Gramos de grasas.
 * @property tipoIngesta Tipo de ingesta (DESAYUNO, COMIDA, MERIENDA, CENA).
 * @property fecha Fecha de la ingesta.
 */
data class Comida(
    val id: String,
    val nombre: String,
    val kcal: Int,
    val proteinasG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val tipoIngesta: String,
    val fecha: LocalDate
)
