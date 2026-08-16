/**
 * @file IngestaRegistrada.kt
 * @brief Modelo de dominio que representa una ingesta realmente consumida por el usuario.
 *
 * A diferencia del [PlanComida] (lo planificado), la ingesta registrada es la cantidad
 * efectivamente consumida y se utiliza como base del cálculo de desvíos nutricionales
 * del módulo de rebalanceo intra-día Naturvitia.
 */
package com.gym.app.domain.model

import java.time.LocalDate

/**
 * @class IngestaRegistrada
 * @brief Representa una ingesta consumida en un momento concreto del día.
 *
 * @property id Identificador único de la ingesta registrada.
 * @property userId Identificador del usuario que registró la ingesta.
 * @property nombre Nombre descriptivo de la ingesta (p. ej. "Ensalada de pollo").
 * @property kcal Kilocalorías totales de la ingesta consumida.
 * @property proteinasG Gramos de proteína consumidos.
 * @property carbohidratosG Gramos de carbohidratos consumidos.
 * @property grasasG Gramos de grasas consumidos.
 * @property tipoIngesta Tipo de ingesta asociada (DESAYUNO, MEDIA_MAÑANA, COMIDA,
 * MERIENDA, CENA, POST_ENTRENO).
 * @property fecha Fecha en la que se consumió la ingesta.
 * @property momentoDia Momento del día en el que se registró (p. ej. "TARDE").
 * @property origen Origen del registro: MANUAL (introducido a mano), IMPORTADA
 * (sincronizada desde el plan) o FOTO (estimada a partir de una imagen).
 */
data class IngestaRegistrada(
    val id: String,
    val userId: String,
    val nombre: String,
    val kcal: Double,
    val proteinasG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val tipoIngesta: String,
    val fecha: LocalDate,
    val momentoDia: String,
    val origen: String
) {

    companion object {
        /** Origen: ingesta introducida manualmente por el usuario. */
        const val ORIGEN_MANUAL: String = "MANUAL"

        /** Origen: ingesta importada o sincronizada desde el plan nutricional. */
        const val ORIGEN_IMPORTADA: String = "IMPORTADA"

        /** Origen: ingesta estimada a partir de una fotografía del plato. */
        const val ORIGEN_FOTO: String = "FOTO"
    }
}