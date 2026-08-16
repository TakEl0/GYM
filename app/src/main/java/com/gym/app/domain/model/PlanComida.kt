/**
 * @file PlanComida.kt
 * @brief Modelo de dominio que representa el plan de comidas (dieta) de un día completo.
 *
 * El plan agrupa las tomas diarias del método Naturvitia (desayuno, media mañana, comida,
 * merienda, cena y post-entreno) y expone propiedades calculadas que agregan las
 * kilocalorías y macronutrientes del día completo.
 */
package com.gym.app.domain.model

import java.time.LocalDate

/**
 * @class PlanComida
 * @brief Representa la dieta de un día concreto, compuesta por varias tomas.
 *
 * @property id Identificador único del plan.
 * @property nombre Nombre descriptivo del plan (p. ej. "Plan Naturvitia Semana 1 - Lunes").
 * @property fecha Fecha a la que corresponde el plan.
 * @property tomas Lista de tomas ordenadas del día.
 * @property origenImportacion Indica si el plan proviene de la importación de un documento
 * del nutricionista (true) o fue creado manualmente por el usuario (false).
 */
data class PlanComida(
    val id: String,
    val nombre: String,
    val fecha: LocalDate,
    val tomas: List<Toma> = emptyList(),
    val origenImportacion: Boolean = true
) {

    /**
     * @brief Kilocalorías totales del plan diario.
     * Suma las kilocalorías de todas las tomas del día.
     * @return Kilocalorías totales del plan.
     */
    val kcalTotales: Double
        get() = tomas.sumOf { it.kcal }

    /**
     * @brief Gramos de proteína totales del plan diario.
     * @return Suma de gramos de proteína de todas las tomas.
     */
    val proteinasTotalesG: Double
        get() = tomas.sumOf { it.proteinasG }

    /**
     * @brief Gramos de carbohidratos totales del plan diario.
     * @return Suma de gramos de carbohidratos de todas las tomas.
     */
    val carbohidratosTotalesG: Double
        get() = tomas.sumOf { it.carbohidratosG }

    /**
     * @brief Gramos de grasas totales del plan diario.
     * @return Suma de gramos de grasas de todas las tomas.
     */
    val grasasTotalesG: Double
        get() = tomas.sumOf { it.grasasG }

    /**
     * @brief Obtiene la toma del plan correspondiente a un tipo de ingesta.
     * @param tipo Tipo de ingesta buscado (p. ej. "COMIDA").
     * @return La [Toma] cuyo tipo coincide con el solicitado, o `null` si no existe.
     */
    fun tomaPorTipo(tipo: String): Toma? = tomas.firstOrNull { it.tipoIngesta == tipo }
}