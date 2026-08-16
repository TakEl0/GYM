/**
 * @file Toma.kt
 * @brief Modelo de dominio que representa una toma (comida) dentro del plan diario.
 *
 * Una toma agrupa varios [IngredienteToma] y corresponde a uno de los momentos de
 * ingesta del método Naturvitia: DESAYUNO, MEDIA_MAÑANA, COMIDA, MERIENDA, CENA o
 * POST_ENTRENO. La clase suma los macros de todos sus ingredientes.
 */
package com.gym.app.domain.model

/**
 * @class Toma
 * @brief Representa una toma diaria compuesta por un conjunto de ingredientes.
 *
 * @property id Identificador único de la toma.
 * @property tipoIngesta Tipo de ingesta (DESAYUNO, MEDIA_MAÑANA, COMIDA, MERIENDA,
 * CENA, POST_ENTRENO).
 * @property orden Posición ordinal de la toma dentro del día (1 = desayuno, etc.).
 * @property ingredientes Lista de ingredientes que componen la toma.
 * @property horaSugerida Hora recomendada de ingesta en formato "HH:mm" (opcional).
 */
data class Toma(
    val id: String,
    val tipoIngesta: String,
    val orden: Int,
    val ingredientes: List<IngredienteToma> = emptyList(),
    val horaSugerida: String? = null
) {

    companion object {
        /** Tipo de ingesta: desayuno. */
        const val TIPO_DESAYUNO: String = "DESAYUNO"

        /** Tipo de ingesta: media mañana. */
        const val TIPO_MEDIA_MAÑANA: String = "MEDIA_MAÑANA"

        /** Tipo de ingesta: comida (almuerzo). */
        const val TIPO_COMIDA: String = "COMIDA"

        /** Tipo de ingesta: merienda. */
        const val TIPO_MERIENDA: String = "MERIENDA"

        /** Tipo de ingesta: cena. */
        const val TIPO_CENA: String = "CENA"

        /** Tipo de ingesta: post entrenamiento. */
        const val TIPO_POST_ENTRENO: String = "POST_ENTRENO"
    }

    /**
     * @brief Kilocalorías totales de la toma.
     * Suma las kilocalorías de todos los ingredientes que la componen.
     * @return Kilocalorías totales de la toma.
     */
    val kcal: Double
        get() = ingredientes.sumOf { it.kcal }

    /**
     * @brief Gramos de proteína totales de la toma.
     * @return Suma de gramos de proteína de todos los ingredientes.
     */
    val proteinasG: Double
        get() = ingredientes.sumOf { it.proteinasG }

    /**
     * @brief Gramos de carbohidratos totales de la toma.
     * @return Suma de gramos de carbohidratos de todos los ingredientes.
     */
    val carbohidratosG: Double
        get() = ingredientes.sumOf { it.carbohidratosG }

    /**
     * @brief Gramos de grasas totales de la toma.
     * @return Suma de gramos de grasas de todos los ingredientes.
     */
    val grasasG: Double
        get() = ingredientes.sumOf { it.grasasG }
}