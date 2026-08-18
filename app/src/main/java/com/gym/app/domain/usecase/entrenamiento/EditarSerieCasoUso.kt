/**
 * @file EditarSerieCasoUso.kt
 * @brief Caso de uso de edición de una serie realizada en la sesión en vivo.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.repository.RepositorioSerieRealizada
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class EditarSerieCasoUso
 * @brief Actualiza la carga (kg) y las repeticiones de una [SerieRealizada] ya
 * registrada, conservando su identificador, número de serie y fecha originales.
 *
 * La persistencia se realiza con [RepositorioSerieRealizada.guardarSerie], que
 * en la implementación Room utiliza `INSERT ... ON CONFLICT REPLACE`, de modo
 * que el mismo id actualiza la fila existente sin duplicarla.
 *
 * # Validaciones (iguales que en el registro)
 * - El peso debe ser estrictamente mayor que 0 kg.
 * - Las repeticiones deben ser al menos 1.
 *
 * @property repositorioSerieRealizada Puerto de persistencia de las series.
 * @property dispatcher Dispatcher sobre el que se ejecuta la edición (por defecto IO).
 */
class EditarSerieCasoUso(
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Actualiza los kilogramos y las repeticiones de una serie existente.
     * @param serieId Identificador de la serie a editar.
     * @param pesoKg Nueva carga en kilogramos (debe ser mayor que 0).
     * @param repeticiones Nuevas repeticiones (deben ser al menos 1).
     * @return [Result] con éxito (Unit) o el error de validación o de persistencia.
     */
    suspend fun ejecutar(
        serieId: String,
        pesoKg: Double,
        repeticiones: Int
    ): Result<Unit> = withContext(dispatcher) {
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
            val serie = repositorioSerieRealizada.obtenerPorId(serieId)
                ?: return@withContext Result.failure(
                    IllegalArgumentException(MENSAJE_SERIE_NO_ENCONTRADA.format(serieId))
                )
            repositorioSerieRealizada.guardarSerie(
                serie.copy(pesoKg = pesoKg, repeticiones = repeticiones)
            )
            Result.success(Unit)
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

        /** Mensaje de error cuando no existe ninguna serie con el id indicado. */
        const val MENSAJE_SERIE_NO_ENCONTRADA: String =
            "No se encontró la serie con id %s."
    }
}