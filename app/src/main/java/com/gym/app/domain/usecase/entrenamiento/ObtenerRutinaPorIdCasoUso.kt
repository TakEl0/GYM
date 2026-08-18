/**
 * @file ObtenerRutinaPorIdCasoUso.kt
 * @brief Caso de uso de consulta de una rutina de entrenamiento por su identificador.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.Rutina
import com.gym.app.domain.repository.RepositorioRutina
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @class ObtenerRutinaPorIdCasoUso
 * @brief Recupera la [Rutina] cuyo identificador coincide con el solicitado.
 *
 * Consulta el flujo reactivo de rutinas del [RepositorioRutina] y devuelve la
 * primera coincidencia exacta por `id`. Si ninguna rutina coincide, devuelve
 * `null` (por ejemplo, cuando el usuario inicia la sesión en vivo desde una
 * rutina que fue eliminada o aún no ha terminado de importarse).
 *
 * @property repositorioRutina Puerto de acceso a las rutinas del usuario.
 * @property dispatcher Dispatcher sobre el que se ejecuta la consulta (por defecto IO).
 */
class ObtenerRutinaPorIdCasoUso(
    private val repositorioRutina: RepositorioRutina,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Obtiene la rutina con el identificador indicado.
     * @param rutinaId Identificador único de la rutina buscada.
     * @return La [Rutina] encontrada, o `null` si no existe ninguna con ese id.
     */
    suspend fun ejecutar(rutinaId: String): Rutina? = withContext(dispatcher) {
        repositorioRutina.observarRutinas()
            .first()
            .firstOrNull { it.id == rutinaId }
    }
}