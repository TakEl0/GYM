/**
 * @file DesvioNutricional.kt
 * @brief Modelo de dominio que cuantifica la diferencia entre lo planificado y lo consumido.
 *
 * Este modelo es la pieza central del módulo de rebalanceo intra-día Naturvitia: a partir
 * del desvío (Δ = planificado - consumido) y de la tolerancia configurada (5 % o ±10 g por
 * macronutriente) decide si el menú necesita ajustes o puede mantenerse sin cambios.
 */
package com.gym.app.domain.model

/**
 * @class DesvioNutricional
 * @brief Representa el desvío nutricional entre las tomas planificadas y las ingestas consumidas.
 *
 * @property proteinasG Desvío de proteína en gramos (positivo = falta consumir, negativo = exceso).
 * @property carbohidratosG Desvío de carbohidratos en gramos.
 * @property grasasG Desvío de grasas en gramos.
 * @property kcal Desvío calórico total en kilocalorías.
 * @property dentroTolerancia Indica si el desvío se encuentra dentro de la tolerancia permitida
 * (5 % o ±10 g por macronutriente) y, por tanto, no requiere ajuste del menú.
 */
data class DesvioNutricional(
    val proteinasG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val kcal: Double,
    val dentroTolerancia: Boolean
) {

    companion object {
        /** Tolerancia absoluta en gramos por macronutriente (±10 g). */
        const val TOLERANCIA_GRAMOS: Double = 10.0

        /** Tolerancia relativa porcentual sobre el planificado (5 %). */
        const val TOLERANCIA_PORCENTAJE: Double = 0.05

        /** Tolerancia absoluta en kilocalorías (±10 kcal). */
        const val TOLERANCIA_KCAL: Double = 10.0

        /**
         * @brief Calcula el desvío nutricional entre una toma planificada y una toma consumida.
         *
         * Para cada macronutriente el desvío se define como Δ = planificado - consumido.
         * El desvío se considera dentro de tolerancia si la diferencia absoluta es menor o
         * igual al mayor entre el 5 % del valor planificado y 10 gramos, evitando así una
         * rigidez innecesaria en el plan (regla del método Naturvitia). El mismo criterio se
         * aplica a las kilocalorías con la constante [TOLERANCIA_KCAL].
         *
         * @param planificado Toma planificada según la dieta (fuente de verdad).
         * @param consumido Toma efectivamente consumida por el usuario.
         * @return [DesvioNutricional] con los desvíos calculados y el veredicto de tolerancia.
         */
        fun calcular(planificado: Toma, consumido: Toma): DesvioNutricional {
            val deltaProteinas = planificado.proteinasG - consumido.proteinasG
            val deltaCarbohidratos = planificado.carbohidratosG - consumido.carbohidratosG
            val deltaGrasas = planificado.grasasG - consumido.grasasG
            val deltaKcal = planificado.kcal - consumido.kcal

            val dentroTolerancia = estaDentroDeTolerancia(
                deltaProteinas,
                planificado.proteinasG
            ) && estaDentroDeTolerancia(
                deltaCarbohidratos,
                planificado.carbohidratosG
            ) && estaDentroDeTolerancia(
                deltaGrasas,
                planificado.grasasG
            ) && kotlin.math.abs(deltaKcal) <= TOLERANCIA_KCAL

            return DesvioNutricional(
                proteinasG = deltaProteinas,
                carbohidratosG = deltaCarbohidratos,
                grasasG = deltaGrasas,
                kcal = deltaKcal,
                dentroTolerancia = dentroTolerancia
            )
        }

        /**
         * @brief Calcula el desvío nutricional agregado entre un conjunto de tomas planificadas
         * y un conjunto de ingestas registradas (por ejemplo, el resumen de todo un día).
         *
         * Los valores planificados se suman a partir de todas las [Toma] y los consumidos a
         * partir de todas las [IngestaRegistrada]. La tolerancia se evalúa comparando cada
         * total agregado con la referencia planificada.
         *
         * @param planificados Lista de tomas planificadas del periodo evaluado.
         * @param consumidas Lista de ingestas registradas como consumidas en el periodo.
         * @return [DesvioNutricional] agregado con el veredicto de tolerancia global.
         */
        fun calcular(
            planificados: List<Toma>,
            consumidas: List<IngestaRegistrada>
        ): DesvioNutricional {
            val proteinaPlanificada = planificados.sumOf { it.proteinasG }
            val chPlanificado = planificados.sumOf { it.carbohidratosG }
            val grasasPlanificadas = planificados.sumOf { it.grasasG }
            val kcalPlanificadas = planificados.sumOf { it.kcal }

            val proteinaConsumida = consumidas.sumOf { it.proteinasG }
            val chConsumido = consumidas.sumOf { it.carbohidratosG }
            val grasasConsumidas = consumidas.sumOf { it.grasasG }
            val kcalConsumidas = consumidas.sumOf { it.kcal }

            val deltaProteinas = proteinaPlanificada - proteinaConsumida
            val deltaCarbohidratos = chPlanificado - chConsumido
            val deltaGrasas = grasasPlanificadas - grasasConsumidas
            val deltaKcal = kcalPlanificadas - kcalConsumidas

            val dentroTolerancia = estaDentroDeTolerancia(
                deltaProteinas,
                proteinaPlanificada
            ) && estaDentroDeTolerancia(
                deltaCarbohidratos,
                chPlanificado
            ) && estaDentroDeTolerancia(
                deltaGrasas,
                grasasPlanificadas
            ) && kotlin.math.abs(deltaKcal) <= TOLERANCIA_KCAL

            return DesvioNutricional(
                proteinasG = deltaProteinas,
                carbohidratosG = deltaCarbohidratos,
                grasasG = deltaGrasas,
                kcal = deltaKcal,
                dentroTolerancia = dentroTolerancia
            )
        }

        /**
         * @brief Evalúa si un desvío concreto de un macronutriente está dentro de tolerancia.
         *
         * El umbral permitido es el mayor entre el 5 % del valor planificado y 10 gramos.
         * Si el valor planificado es 0, se usa únicamente la tolerancia absoluta de 10 g.
         * @param delta Desvío calculado (planificado - consumido).
         * @param planificado Valor planificado del macronutriente en gramos.
         * @return `true` si la diferencia absoluta está dentro del umbral permitido.
         */
        private fun estaDentroDeTolerancia(delta: Double, planificado: Double): Boolean {
            val umbralPorcentual = kotlin.math.abs(planificado) * TOLERANCIA_PORCENTAJE
            val umbral = maxOf(umbralPorcentual, TOLERANCIA_GRAMOS)
            return kotlin.math.abs(delta) <= umbral
        }
    }
}