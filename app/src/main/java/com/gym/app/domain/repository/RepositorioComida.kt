/**
 * @file RepositorioComida.kt
 * @brief Puerto de repositorio para la gestión de comidas y nutrición.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.Comida
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * @interface RepositorioComida
 * @brief Contrato para el acceso y sincronización de registros de comidas del usuario.
 */
interface RepositorioComida {

    /**
     * @brief Observa las comidas registradas para una fecha concreta.
     * @param fecha Fecha de consulta.
     * @return Flow con la lista de [Comida] para dicha fecha.
     */
    fun observarComidasPorFecha(fecha: LocalDate): Flow<List<Comida>>

    /**
     * @brief Guarda o actualiza un registro de comida.
     * @param comida Objeto [Comida] a guardar.
     */
    suspend fun guardarComida(comida: Comida)

    /**
     * @brief Elimina un registro de comida por su identificador.
     * @param id Identificador único de la comida.
     */
    suspend fun eliminarComida(id: String)

    /**
     * @brief Sincroniza las comidas pendientes con el backend remoto de Supabase.
     * @return Result con éxito o error.
     */
    suspend fun sincronizarConRemoto(): Result<Unit>
}
