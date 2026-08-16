/**
 * @file ObservarSesionesEntrenamientoCasoUso.kt
 * @brief Caso de uso de observación reactiva de las sesiones de entrenamiento de un rango.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId

/**
 * @class ObservarSesionesEntrenamientoCasoUso
 * @brief Expone un flujo reactivo con las [SesionEntrenamiento] comprendidas en
 * un rango de fechas, para alimentar el calendario de entrenamientos y el
 * seguimiento del volumen semanal.
 *
 * Convierte los límites expresados en milisegundos desde la época (epoch) a
 * [java.time.LocalDate] para adaptarse a la firma del
 * [RepositorioSesionEntrenamiento.observarSesiones], siguiendo el mismo patrón
 * que el resto de casos de uso de observación del dominio.
 */
class ObservarSesionesEntrenamientoCasoUso(
    private val repositorioSesionEntrenamiento: RepositorioSesionEntrenamiento
) {

    /**
     * @brief Observa las sesiones de entrenamiento dentro del rango indicado.
     * @param inicio Epoch millis del primer día del rango (inclusive).
     * @param fin Epoch millis del último día del rango (inclusive).
     * @return [Flow] que emite la lista de [SesionEntrenamiento] del rango,
     * actualizándose ante cambios en la fuente de datos.
     */
    fun ejecutar(inicio: Long, fin: Long): Flow<List<SesionEntrenamiento>> {
        val inicioFecha = Instant.ofEpochMilli(inicio)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val finFecha = Instant.ofEpochMilli(fin)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return repositorioSesionEntrenamiento.observarSesiones(inicioFecha, finFecha)
    }
}