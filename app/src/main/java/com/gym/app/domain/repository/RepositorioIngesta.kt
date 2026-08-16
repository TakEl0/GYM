/**
 * @file RepositorioIngesta.kt
 * @brief Puerto de repositorio de ingestas registradas en la capa de dominio.
 * Define el contrato para registrar lo realmente consumido por el usuario, consultar
 * las ingestas de un día y sincronizarlas con el backend remoto de Supabase.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.IngestaRegistrada
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * @interface RepositorioIngesta
 * @brief Contrato de acceso a los registros de ingesta consumida por el usuario.
 * Estas ingestas son la base del cálculo de desvíos nutricionales del rebalanceo
 * intra-día Naturvitia.
 */
interface RepositorioIngesta {

    /**
     * @brief Observa de forma reactiva las ingestas registradas en una fecha concreta.
     * @param fecha Fecha de consulta.
     * @return Flujo reactivo con la lista de [IngestaRegistrada] de esa fecha.
     */
    fun observarIngestasDelDia(fecha: LocalDate): Flow<List<IngestaRegistrada>>

    /**
     * @brief Registra una nueva ingesta consumida por el usuario.
     * @param ingesta Ingesta a persistir.
     */
    suspend fun registrarIngesta(ingesta: IngestaRegistrada)

    /**
     * @brief Elimina una ingesta registrada por su identificador.
     * @param id Identificador único de la ingesta.
     */
    suspend fun eliminarIngesta(id: String)

    /**
     * @brief Sincroniza las ingestas pendientes con el backend remoto de Supabase.
     * @return `Result` con éxito si la sincronización se completó, o un error
     * descriptivo en caso contrario.
     */
    suspend fun sincronizarConRemoto(): Result<Unit>
}