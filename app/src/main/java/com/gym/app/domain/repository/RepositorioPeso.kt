/**
 * @file RepositorioPeso.kt
 * @brief Puerto de repositorio de peso corporal en la capa de dominio.
 * Define el contrato para guardar y consultar los registros de peso del
 * usuario, usados para monitorizar su recomposición corporal.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.RegistroPeso
import java.time.LocalDate

/**
 * @interface RepositorioPeso
 * @brief Contrato de acceso a los registros de peso corporal.
 * La capa de dominio depende de esta abstracción; la implementación concreta
 * puede usar Room, Health Connect o Supabase de forma intercambiable.
 */
interface RepositorioPeso {

    /**
     * @brief Obtiene el historial de registros de peso ordenado por fecha.
     * @return Lista de registros de peso del más reciente al más antiguo.
     */
    suspend fun obtenerHistorial(): List<RegistroPeso>

    /**
     * @brief Guarda un nuevo registro de peso en el sistema.
     * @param registro Registro de peso a almacenar.
     */
    suspend fun guardarRegistro(registro: RegistroPeso)

    /**
     * @brief Obtiene el último registro de peso disponible.
     * @return Último registro de peso, o null si no existe ninguno.
     */
    suspend fun obtenerUltimoRegistro(): RegistroPeso?

    /**
     * @brief Obtiene el peso registrado en una fecha concreta.
     * @param fecha Fecha de la medición a consultar.
     * @return Registro de peso de la fecha indicada, o null si no existe.
     */
    suspend fun obtenerPesoEnFecha(fecha: LocalDate): RegistroPeso?
}