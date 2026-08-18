/**
 * @file EliminarSerieCasoUso.kt
 * @brief Caso de uso de eliminación de una serie realizada y renumeración del resto.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.repository.RepositorioSerieRealizada
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @class EliminarSerieCasoUso
 * @brief Elimina una [SerieRealizada] de la sesión activa y renumeración las
 * series restantes de esa misma sesión para que queden consecutivas (1..N).
 *
 * # Flujo de ejecución
 * 1. Localiza la serie a eliminar (si no existe, devuelve error).
 * 2. La elimina con [RepositorioSerieRealizada.eliminarSerie].
 * 3. Recupera todas las series restantes de la sesión, las ordena por su
 *    `numeroSerie` y las vuelve a guardar con números consecutivos (1, 2, 3...)
 *    para que el contador de la tarjeta del ejercicio siga siendo coherente
 *    (p. ej. tras eliminar la 2ª serie, la antigua 3ª pasa a ser la 2ª).
 *
 * @property repositorioSerieRealizada Puerto de persistencia de las series.
 * @property dispatcher Dispatcher sobre el que se ejecuta la eliminación (por defecto IO).
 */
class EliminarSerieCasoUso(
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Elimina una serie y renumera las restantes de la sesión.
     * @param serieId Identificador de la serie a eliminar.
     * @return [Result] con éxito (Unit) o el error de localización o de persistencia.
     */
    suspend fun ejecutar(serieId: String): Result<Unit> = withContext(dispatcher) {
        try {
            val serie = repositorioSerieRealizada.obtenerPorId(serieId)
                ?: return@withContext Result.failure(
                    IllegalArgumentException(MENSAJE_SERIE_NO_ENCONTRADA.format(serieId))
                )

            repositorioSerieRealizada.eliminarSerie(serieId)

            val restantes = repositorioSerieRealizada
                .observarPorSesion(serie.sesionId)
                .first()
                .sortedBy { it.numeroSerie }

            restantes.forEachIndexed { indice, restante ->
                val numeroConsecutivo = indice + 1
                if (restante.numeroSerie != numeroConsecutivo) {
                    repositorioSerieRealizada.guardarSerie(
                        restante.copy(numeroSerie = numeroConsecutivo)
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /** Mensaje de error cuando no existe ninguna serie con el id indicado. */
        const val MENSAJE_SERIE_NO_ENCONTRADA: String =
            "No se encontró la serie con id %s."
    }
}