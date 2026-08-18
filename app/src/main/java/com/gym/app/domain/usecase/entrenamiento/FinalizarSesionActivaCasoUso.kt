/**
 * @file FinalizarSesionActivaCasoUso.kt
 * @brief Caso de uso de finalización de una sesión de entrenamiento en vivo.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSerieRealizada
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @class FinalizarSesionActivaCasoUso
 * @brief Cierra la sesión activa persistiendo una [SesionEntrenamiento] resumida
 * a partir de todas las series registradas durante la sesión.
 *
 * # Resumen construido
 * - `ejerciciosCompletados`: identificadores únicos de los ejercicios con al
 *   menos una serie registrada.
 * - `serieRealizadas`: número total de series de la sesión.
 * - `fecha`: instante de finalización en epoch millis.
 * - `completo`: siempre `true` (la sesión finaliza de forma explícita).
 *
 * El id de la sesión se reutiliza (`sesionId`), de modo que la sesión guardada
 * queda vinculada a las series registradas durante el entrenamiento en vivo.
 *
 * @property repositorioSerieRealizada Puerto de acceso a las series de la sesión.
 * @property repositorioSesionEntrenamiento Puerto de persistencia de las sesiones.
 * @property dispatcher Dispatcher sobre el que se ejecuta la finalización (por defecto IO).
 */
class FinalizarSesionActivaCasoUso(
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val repositorioSesionEntrenamiento: RepositorioSesionEntrenamiento,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Finaliza la sesión activa y persiste su resumen.
     * @param sesionId Identificador de la sesión activa (se reutiliza como id de
     * la [SesionEntrenamiento] guardada).
     * @param nombreRutina Nombre de la rutina ejecutada en la sesión.
     * @param userId Identificador del usuario que realizó la sesión.
     * @param duracionMinutos Duración real de la sesión en minutos (no puede ser negativa).
     * @return [Result] con éxito (Unit) o el error de validación o de persistencia.
     */
    suspend fun ejecutar(
        sesionId: String,
        nombreRutina: String,
        userId: String,
        duracionMinutos: Int
    ): Result<Unit> = withContext(dispatcher) {
        if (duracionMinutos < 0) {
            return@withContext Result.failure(
                IllegalArgumentException(MENSAJE_DURACION_INVALIDA)
            )
        }
        try {
            val series = repositorioSerieRealizada.observarPorSesion(sesionId).first()
            val sesion = SesionEntrenamiento(
                id = sesionId,
                userId = userId,
                fecha = System.currentTimeMillis(),
                nombreRutina = nombreRutina,
                ejerciciosCompletados = series.map { it.ejercicioId }.distinct(),
                serieRealizadas = series.size,
                duracionMinutos = duracionMinutos,
                completo = true
            )
            repositorioSesionEntrenamiento.guardarSesion(sesion)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /** Mensaje de error cuando la duración de la sesión es negativa. */
        const val MENSAJE_DURACION_INVALIDA: String =
            "La duración de la sesión no puede ser negativa."
    }
}