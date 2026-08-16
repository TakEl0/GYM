/**
 * @file CalcularResumenNutricionalCasoUso.kt
 * @brief Caso de uso de cálculo del resumen nutricional diario (lógica pura).
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Comida
import com.gym.app.domain.model.ResumenNutricional

/**
 * @class CalcularResumenNutricionalCasoUso
 * @brief Agrega las comidas del día y calcula los totales consumidos frente a los
 * objetivos del plan nutricional (método Naturvitia).
 *
 * Es un caso de uso de lógica pura: no depende de repositorios ni de frameworks,
 * por lo que es trivialmente testeable con datos en memoria.
 */
class CalcularResumenNutricionalCasoUso {

    /**
     * @brief Calcula el [ResumenNutricional] a partir de las comidas consumidas.
     * Suma las kilocalorías y los macronutrientes de todas las [Comida] y calcula
     * las kilocalorías restantes como objetivo menos consumido.
     * @param comidas Lista de comidas registradas en el día.
     * @param kcalObjetivo Kilocalorías objetivo del plan nutricional.
     * @param proteinasObjetivoG Gramos de proteína objetivo.
     * @param carbohidratosObjetivoG Gramos de carbohidratos objetivo.
     * @param grasasObjetivoG Gramos de grasas objetivo.
     * @return [ResumenNutricional] con los consumidos, objetivos y restantes.
     */
    fun ejecutar(
        comidas: List<Comida>,
        kcalObjetivo: Double,
        proteinasObjetivoG: Double,
        carbohidratosObjetivoG: Double,
        grasasObjetivoG: Double
    ): ResumenNutricional {
        val kcalConsumidas = comidas.sumOf { it.kcal.toDouble() }
        val proteinasConsumidasG = comidas.sumOf { it.proteinasG }
        val carbohidratosConsumidosG = comidas.sumOf { it.carbohidratosG }
        val grasasConsumidasG = comidas.sumOf { it.grasasG }

        return ResumenNutricional(
            kcalConsumidas = kcalConsumidas,
            kcalObjetivo = kcalObjetivo,
            kcalRestantes = kcalObjetivo - kcalConsumidas,
            proteinasConsumidasG = proteinasConsumidasG,
            proteinasObjetivoG = proteinasObjetivoG,
            carbohidratosConsumidosG = carbohidratosConsumidosG,
            carbohidratosObjetivoG = carbohidratosObjetivoG,
            grasasConsumidasG = grasasConsumidasG,
            grasasObjetivoG = grasasObjetivoG
        )
    }
}