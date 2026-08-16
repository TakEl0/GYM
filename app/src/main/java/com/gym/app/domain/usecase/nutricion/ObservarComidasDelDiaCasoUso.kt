/**
 * @file ObservarComidasDelDiaCasoUso.kt
 * @brief Caso de uso de observación reactiva de las comidas registradas en un día.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Comida
import com.gym.app.domain.repository.RepositorioComida
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId

/**
 * @class ObservarComidasDelDiaCasoUso
 * @brief Expone un flujo reactivo con las [Comida] registradas para un día concreto,
 * delegando la consulta en el [RepositorioComida].
 */
class ObservarComidasDelDiaCasoUso(
    private val repositorioComida: RepositorioComida
) {

    /**
     * @brief Observa las comidas registradas en la fecha indicada.
     * Convierte la fecha expresada en milisegundos desde la época (epoch) a un
     * [java.time.LocalDate] para adaptarse a la firma del repositorio. El
     * [userId] se conserva por coherencia del contrato; la implementación concreta
     * resuelve el usuario activo internamente.
     * @param userId Identificador del usuario propietario de las comidas.
     * @param fecha Fecha de consulta en milisegundos (epoch).
     * @return [Flow] que emite la lista de [Comida] de la fecha indicada.
     */
    fun ejecutar(userId: String, fecha: Long): Flow<List<Comida>> {
        val fechaLocal = Instant.ofEpochMilli(fecha).atZone(ZoneId.systemDefault()).toLocalDate()
        return repositorioComida.observarComidasPorFecha(fechaLocal)
    }
}