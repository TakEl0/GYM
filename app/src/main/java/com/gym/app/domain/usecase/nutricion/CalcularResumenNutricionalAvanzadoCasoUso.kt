/**
 * @file CalcularResumenNutricionalAvanzadoCasoUso.kt
 * @brief Caso de uso de cálculo del resumen nutricional avanzado a partir de ingestas registradas.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.ResumenNutricional

/**
 * @class CalcularResumenNutricionalAvanzadoCasoUso
 * @brief Construye el [ResumenNutricional] del día partiendo de un resumen de
 * objetivos (generado por [CalcularObjetivosNutricionalesCasoUso]) y de la lista
 * de ingestas realmente consumidas.
 *
 * Los valores consumidos se suman desde las [IngestaRegistrada]. Las kilocalorías
 * restantes se calculan como objetivo - consumido y **pueden ser negativas** si
 * el usuario supera su objetivo calórico; el porcentaje de progreso lo resuelve
 * la propiedad calculada del propio modelo (limitada a 100).
 *
 * Es un caso de uso de lógica pura (sin repositorios), trivialmente testeable.
 */
class CalcularResumenNutricionalAvanzadoCasoUso {

    /**
     * @brief Calcula el resumen nutricional avanzado del día.
     * @param objetivos Resumen de objetivos del día (consumidos a cero).
     * @param ingestas Ingestas registradas como consumidas durante el día.
     * @return [Result] con el [ResumenNutricional] calculado.
     */
    suspend fun ejecutar(
        objetivos: ResumenNutricional,
        ingestas: List<IngestaRegistrada>
    ): Result<ResumenNutricional> {
        val kcalConsumidas = ingestas.sumOf { it.kcal }
        val proteinasConsumidasG = ingestas.sumOf { it.proteinasG }
        val carbohidratosConsumidosG = ingestas.sumOf { it.carbohidratosG }
        val grasasConsumidasG = ingestas.sumOf { it.grasasG }

        return Result.success(
            ResumenNutricional(
                kcalConsumidas = kcalConsumidas,
                kcalObjetivo = objetivos.kcalObjetivo,
                kcalRestantes = objetivos.kcalObjetivo - kcalConsumidas,
                proteinasConsumidasG = proteinasConsumidasG,
                proteinasObjetivoG = objetivos.proteinasObjetivoG,
                carbohidratosConsumidosG = carbohidratosConsumidosG,
                carbohidratosObjetivoG = objetivos.carbohidratosObjetivoG,
                grasasConsumidasG = grasasConsumidasG,
                grasasObjetivoG = objetivos.grasasObjetivoG
            )
        )
    }
}