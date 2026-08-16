/**
 * @file SincronizarNutricionEntrenamientoCasoUso.kt
 * @brief Caso de uso de sincronización entre el plan nutricional y la programación de entrenamientos.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Rutina

/**
 * @class SincronizarNutricionEntrenamientoCasoUso
 * @brief Aplica la heurística de sincronización nutrición-entrenamiento del
 * método Naturvitia a partir de los planes de comidas de la semana:
 *
 * - Los días con mayor ingesta calórica **y** de carbohidratos se consideran de
 *   **alta demanda** y se sugiere programar en ellos las sesiones de pierna o
 *   espalda (los grupos musculares que más glucógeno consumen).
 * - Los días con menor ingesta calórica se consideran de **baja demanda** y se
 *   sugiere reservarlos para cardio, movilidad o descanso.
 *
 * El caso de uso devuelve sugerencias en texto legible en castellano. Si no se
 * recibe ningún plan, devuelve una lista vacía. La lista de rutinas se acepta
 * para enriquecer la heurística en el futuro (p. ej. comprobar volumen semanal),
 * aunque la regla actual solo depende de los planes de comidas.
 *
 * Es un caso de uso de lógica pura (sin repositorios), trivialmente testeable.
 */
class SincronizarNutricionEntrenamientoCasoUso {

    /**
     * @brief Calcula las sugerencias de programación semanal de entrenamientos.
     * @param planes Planes de comidas de la semana (lunes a domingo).
     * @param rutinas Rutinas de entrenamiento configuradas (reservado para
     * enriquecimiento futuro de la heurística).
     * @return [Result] con la lista de sugerencias en castellano (vacía si no
     * hay planes o no hay datos suficientes).
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun ejecutar(
        planes: List<PlanComida>,
        rutinas: List<Rutina>
    ): Result<List<String>> {
        if (planes.isEmpty()) return Result.success(emptyList())

        // Acumulación de kilocalorías y carbohidratos por día de la semana (1..7).
        val kcalPorDia = DoubleArray(DIAS_SEMANA + 1)
        val chPorDia = DoubleArray(DIAS_SEMANA + 1)
        for (plan in planes) {
            val dia = plan.fecha.dayOfWeek.value
            if (dia in 1..DIAS_SEMANA) {
                kcalPorDia[dia] += plan.kcalTotales
                chPorDia[dia] += plan.carbohidratosTotalesG
            }
        }

        val diasConDatos = (1..DIAS_SEMANA).filter { kcalPorDia[it] > 0.0 }
        if (diasConDatos.isEmpty()) return Result.success(emptyList())

        val kcalMaxima = diasConDatos.maxOf { kcalPorDia[it] }
        val kcalMinima = diasConDatos.minOf { kcalPorDia[it] }

        // Alta demanda: entre los días de máxima kcal, los que además tienen más CH.
        val diasMaxKcal = diasConDatos.filter { kcalPorDia[it] == kcalMaxima }
        val chMaxima = diasMaxKcal.maxOf { chPorDia[it] }
        val diasAltaDemanda = diasMaxKcal.filter { chPorDia[it] == chMaxima }

        // Baja demanda: días de mínima kcal que no sean también de alta demanda.
        val diasBajaDemanda = diasConDatos
            .filter { kcalPorDia[it] == kcalMinima }
            .filter { it !in diasAltaDemanda }

        val sugerencias = mutableListOf<String>()

        if (diasAltaDemanda.isNotEmpty()) {
            val nombres = diasAltaDemanda.sorted().joinToString(" y ") { nombreDia(it) }
            sugerencias +=
                "Programa las sesiones de alta demanda (pierna/espalda) el $nombres."
        }
        if (diasBajaDemanda.isNotEmpty()) {
            val nombres = diasBajaDemanda.sorted().joinToString(" y ") { nombreDia(it) }
            sugerencias += "Reserva $nombres para cardio, movilidad o descanso."
        }

        return Result.success(sugerencias)
    }

    /**
     * @brief Devuelve el nombre del día de la semana en mayúsculas.
     * @param diaSemana Día de la semana (1 = lunes ... 7 = domingo).
     * @return Nombre del día en mayúsculas (p. ej. "LUNES").
     */
    private fun nombreDia(diaSemana: Int): String = when (diaSemana) {
        1 -> "LUNES"
        2 -> "MARTES"
        3 -> "MIÉRCOLES"
        4 -> "JUEVES"
        5 -> "VIERNES"
        6 -> "SÁBADO"
        else -> "DOMINGO"
    }

    companion object {
        /** Número de días de la semana evaluados (lunes a domingo). */
        private const val DIAS_SEMANA: Int = 7
    }
}