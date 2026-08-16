/**
 * @file RepositorioSesionEntrenamiento.kt
 * @brief Puerto de repositorio de sesiones de entrenamiento en la capa de dominio.
 * Define el contrato para observar las sesiones realizadas en un rango de fechas,
 * guardar nuevas sesiones y contar las sesiones completadas en una semana para la
 * sincronización nutrición-entrenamiento.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.SesionEntrenamiento
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * @interface RepositorioSesionEntrenamiento
 * @brief Contrato de acceso a las sesiones de entrenamiento realizadas por el usuario.
 */
interface RepositorioSesionEntrenamiento {

    /**
     * @brief Observa de forma reactiva las sesiones de entrenamiento dentro de un rango.
     * @param inicio Fecha inicial del rango (incluida).
     * @param fin Fecha final del rango (incluida).
     * @return Flujo reactivo con la lista de [SesionEntrenamiento] del rango solicitado.
     */
    fun observarSesiones(inicio: LocalDate, fin: LocalDate): Flow<List<SesionEntrenamiento>>

    /**
     * @brief Guarda o actualiza una sesión de entrenamiento.
     * @param sesion Sesión a persistir.
     */
    suspend fun guardarSesion(sesion: SesionEntrenamiento)

    /**
     * @brief Cuenta las sesiones completadas en la semana que contiene la fecha indicada.
     * Se utiliza para evaluar el volumen semanal y decidir si el día requiere carga alta
     * o baja en función de la ingesta calórica planificada.
     * @param fecha Fecha perteneciente a la semana a evaluar.
     * @return Número de sesiones marcadas como completas en esa semana.
     */
    suspend fun sesionesCompletadasSemana(fecha: LocalDate): Int
}