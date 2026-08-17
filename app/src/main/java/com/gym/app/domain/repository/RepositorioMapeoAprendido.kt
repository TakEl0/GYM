/**
 * @file RepositorioMapeoAprendido.kt
 * @brief Puerto de repositorio del aprendizaje de mapeos ejercicio → máquina en la capa
 * de dominio. Define el contrato para guardar y consultar las correcciones manuales del
 * usuario sin depender de la tecnología de persistencia (Room, Supabase o memoria).
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.MapeoAprendido
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioMapeoAprendido
 * @brief Contrato de acceso a los mapeos aprendidos por el usuario.
 */
interface RepositorioMapeoAprendido {

    /**
     * @brief Guarda (o reemplaza) un mapeo aprendido. Si ya existe uno con el mismo
     * [MapeoAprendido.nombreNormalizado], la corrección más reciente gana.
     * @param m Mapeo aprendido a persistir.
     */
    suspend fun guardar(m: MapeoAprendido)

    /**
     * @brief Busca el mapeo aprendido para un nombre de ejercicio normalizado.
     * @param nombreNormalizado Clave de búsqueda (nombre ya normalizado).
     * @return [MapeoAprendido] si existe, o `null` si el usuario nunca lo corrigió.
     */
    suspend fun buscar(nombreNormalizado: String): MapeoAprendido?

    /**
     * @brief Observa de forma reactiva todos los mapeos aprendidos.
     * @return Flujo reactivo con la lista completa de mapeos aprendidos.
     */
    fun observar(): Flow<List<MapeoAprendido>>
}