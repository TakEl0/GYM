/**
 * @file CalcularResumenSesionCasoUso.kt
 * @brief Caso de uso de cálculo del resumen estadístico de una sesión finalizada.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.CalculoUnRM
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioSerieRealizada
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @data class EjercicioUnRM
 * @brief Estimación del 1RM de un ejercicio a partir de su mejor serie de la sesión.
 *
 * @property ejercicioId Identificador del ejercicio.
 * @property nombre Nombre legible del ejercicio (resuelto desde el catálogo).
 * @property mejorSeriePesoKg Carga de la serie con mayor peso de la sesión.
 * @property mejorSerieReps Repeticiones de la mejor serie (desempate de la selección).
 * @property estimacionUnRM Estimación del máximo de una repetición calculada con
 * [CalculoUnRM.calcular] (promedio de Epley y Brzycki).
 */
data class EjercicioUnRM(
    val ejercicioId: String,
    val nombre: String,
    val mejorSeriePesoKg: Double,
    val mejorSerieReps: Int,
    val estimacionUnRM: Double
)

/**
 * @data class ResumenSesion
 * @brief Resumen estadístico de una sesión de entrenamiento en vivo.
 *
 * @property volumenTotalKg Volumen total levantado: Σ (pesoKg × repeticiones).
 * @property seriesTotales Número total de series registradas en la sesión.
 * @property ejerciciosConUnRM Estimación de 1RM por cada ejercicio con series.
 */
data class ResumenSesion(
    val volumenTotalKg: Double,
    val seriesTotales: Int,
    val ejerciciosConUnRM: List<EjercicioUnRM>
)

/**
 * @class CalcularResumenSesionCasoUso
 * @brief Calcula el resumen que muestra la pantalla de fin de sesión.
 *
 * # Estadísticas calculadas
 * 1. **Volumen total**: Σ (pesoKg × repeticiones) de todas las series de la sesión.
 * 2. **Series totales**: número de series registradas.
 * 3. **1RM por ejercicio**: las series se agrupan por `ejercicioId`; para cada
 *    grupo se selecciona la serie con mayor peso (desempate: mayor repeticiones)
 *    y se estima el 1RM con [CalculoUnRM.calcular] (Epley/Brzycki promediados).
 *
 * Los nombres de los ejercicios se resuelven desde el catálogo
 * ([RepositorioEjercicio.observarEjercicios]); si un ejercicio ya no está en el
 * catálogo se usa su identificador como nombre.
 *
 * @property repositorioSerieRealizada Puerto de acceso a las series de la sesión.
 * @property repositorioEjercicio Puerto del catálogo de ejercicios (para nombres).
 * @property dispatcher Dispatcher sobre el que se ejecuta el cálculo (por defecto IO).
 */
class CalcularResumenSesionCasoUso(
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val repositorioEjercicio: RepositorioEjercicio,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Calcula el resumen estadístico de la sesión indicada.
     * @param sesionId Identificador de la sesión finalizada.
     * @return [Result] con el [ResumenSesion] calculado, o el error de lectura.
     */
    suspend fun ejecutar(sesionId: String): Result<ResumenSesion> =
        withContext(dispatcher) {
            try {
                val series = repositorioSerieRealizada.observarPorSesion(sesionId).first()
                val nombresPorId = repositorioEjercicio
                    .observarEjercicios()
                    .first()
                    .associate { it.id to it.nombre }

                val resumen = ResumenSesion(
                    volumenTotalKg = series.sumOf { it.pesoKg * it.repeticiones },
                    seriesTotales = series.size,
                    ejerciciosConUnRM = calcularUnRMPorEjercicio(series, nombresPorId)
                )
                Result.success(resumen)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * @brief Agrupa las series por ejercicio y estima el 1RM de cada uno.
     * @param series Series de la sesión.
     * @param nombresPorId Mapa de identificador de ejercicio a nombre legible.
     * @return Lista de [EjercicioUnRM], una por cada ejercicio con series.
     */
    private fun calcularUnRMPorEjercicio(
        series: List<SerieRealizada>,
        nombresPorId: Map<String, String>
    ): List<EjercicioUnRM> =
        series
            .groupBy { it.ejercicioId }
            .map { (ejercicioId, seriesDelEjercicio) ->
                // Mejor serie: mayor peso y, en caso de empate, mayor repeticiones.
                val mejorSerie = seriesDelEjercicio.maxWith(
                    compareBy<SerieRealizada> { it.pesoKg }.thenBy { it.repeticiones }
                )
                EjercicioUnRM(
                    ejercicioId = ejercicioId,
                    nombre = nombresPorId[ejercicioId] ?: ejercicioId,
                    mejorSeriePesoKg = mejorSerie.pesoKg,
                    mejorSerieReps = mejorSerie.repeticiones,
                    estimacionUnRM = CalculoUnRM.calcular(
                        pesoKg = mejorSerie.pesoKg,
                        repeticiones = mejorSerie.repeticiones
                    )
                )
            }
}