/**
 * @file DaoSesionEntrenamiento.kt
 * @brief DAO para la gestión de sesiones de entrenamiento realizadas.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadSesionEntrenamiento
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoSesionEntrenamiento
 * @brief Operaciones de base de datos para las sesiones de entrenamiento.
 */
@Dao
interface DaoSesionEntrenamiento {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sesion: EntidadSesionEntrenamiento)

    @Query("SELECT * FROM sesion_entrenamiento WHERE userId = :userId AND fecha >= :inicio AND fecha <= :fin ORDER BY fecha ASC")
    fun observarSesiones(userId: String, inicio: Long, fin: Long): Flow<List<EntidadSesionEntrenamiento>>

    @Query("SELECT COUNT(*) FROM sesion_entrenamiento WHERE userId = :userId AND fecha >= :inicioSemana AND fecha <= :finSemana AND completo = 1")
    suspend fun contarCompletadasSemana(userId: String, inicioSemana: Long, finSemana: Long): Int

    @Query("SELECT * FROM sesion_entrenamiento WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EntidadSesionEntrenamiento>

    @Query("UPDATE sesion_entrenamiento SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)
}