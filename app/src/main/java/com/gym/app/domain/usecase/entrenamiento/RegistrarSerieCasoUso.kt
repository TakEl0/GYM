/**
 * @file RegistrarSerieCasoUso.kt
 * @brief Caso de uso de registro de una serie realizada en la sesión en vivo.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioSerieRealizada
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * @class RegistrarSerieCasoUso
 * @brief Valida y persiste una nueva [SerieRealizada] de la sesión activa.
 *
 * # Validaciones
 * - El peso debe ser estrictamente mayor que 0 kg.
 * - Las repeticiones deben ser al menos 1.
 *
 * # Numeración automática
 * El número de serie se calcula como el número de series ya registradas de ese
 * mismo ejercicio dentro de la sesión más uno (1ª serie, 2ª serie, ...). El id
 * de la serie se genera con UUID y la fecha de registro es el instante actual.
 *
 * @property repositorioSerieRealizada Puerto de persistencia de las series.
 * @property dispatcher Dispatcher sobre el que se ejecuta el registro (por defecto IO).
 */
class RegistrarSerieCasoUso(
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra una serie realizada y la persiste en el repositorio.
     * @param sesionId Identificador de la sesión activa a la que pertenece la serie.
     * @param ejercicioId Identificador del ejercicio ejecutado.
     * @param pesoKg Carga levantada en kilogramos (debe ser mayor que 0).
     * @param repeticiones Repeticiones ejecutadas (deben ser al menos 1).
     * @return [Result] con la [SerieRealizada] creada (con su numeroSerie asignado),
     * o el error de validación o de persistencia.
     */
    suspend fun ejecutar(
        sesionId: String,
        ejercicioId: String,
        pesoKg: Double,
        repeticiones: Int
    ): Result<SerieRealizada> = withContext(dispatcher) {
        if (pesoKg <= 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException(MENSAJE_PESO_INVALIDO)
            )
        }
        if (repeticiones < 1) {
            return@withContext Result.failure(
                IllegalArgumentException(MENSAJE_REPETICIONES_INVALIDAS)
            )
        }
        try {
            val seriesDelEjercicio = repositorioSerieRealizada
                .observarPorSesion(sesionId)
                .first()
                .filter { it.ejercicioId == ejercicioId }

            val serie = SerieRealizada(
                id = UUID.randomUUID().toString(),
                sesionId = sesionId,
                ejercicioId = ejercicioId,
                numeroSerie = seriesDelEjercicio.size + 1,
                pesoKg = pesoKg,
                repeticiones = repeticiones,
                fecha = System.currentTimeMillis()
            )
            repositorioSerieRealizada.guardarSerie(serie)
            Result.success(serie)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /** Mensaje de error cuando el peso no es estrictamente positivo. */
        const val MENSAJE_PESO_INVALIDO: String = "El peso debe ser mayor que 0 kg."

        /** Mensaje de error cuando las repeticiones son menores que 1. */
        const val MENSAJE_REPETICIONES_INVALIDAS: String =
            "Las repeticiones deben ser al menos 1."
    }
}