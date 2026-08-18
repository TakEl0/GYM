/**
 * @file CalcularCargaSugeridaCasoUso.kt
 * @brief Caso de uso de cálculo de la carga sugerida para un ejercicio.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.repository.RepositorioSerieRealizada
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class CalcularCargaSugeridaCasoUso
 * @brief Consulta la carga sugerida (en kg) para el diálogo de registro de serie.
 *
 * La sugerencia se obtiene del último peso registrado para ese ejercicio en
 * sesiones anteriores ([RepositorioSerieRealizada.ultimoPesoPorEjercicio]).
 * Si no existe historial previo, devuelve `null` y la interfaz usará el peso del
 * bloque de la rutina si está definido o un valor por defecto (p. ej. 20 kg).
 *
 * @property repositorioSerieRealizada Puerto de acceso al historial de series.
 * @property dispatcher Dispatcher sobre el que se ejecuta la consulta (por defecto IO).
 */
class CalcularCargaSugeridaCasoUso(
    private val repositorioSerieRealizada: RepositorioSerieRealizada,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Obtiene el último peso registrado para el ejercicio indicado.
     * @param ejercicioId Identificador del ejercicio del que se consulta la carga.
     * @return El último peso en kg (si hay historial), o `null` si el ejercicio
     * nunca se ha registrado.
     */
    suspend fun ejecutar(ejercicioId: String): Double? = withContext(dispatcher) {
        repositorioSerieRealizada.ultimoPesoPorEjercicio(ejercicioId)
    }
}