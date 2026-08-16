/**
 * @file DaoEntrenamiento.kt
 * @brief DAO para la gestión de entrenamientos en Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadEntrenamiento
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoEntrenamiento
 * @brief Operaciones de base de datos para rutinas y sesiones de entrenamiento.
 */
@Dao
interface DaoEntrenamiento {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entrenamiento: EntidadEntrenamiento)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(entrenamientos: List<EntidadEntrenamiento>)

    @Query("SELECT * FROM entrenamientos WHERE userId = :userId")
    fun observarPorUsuario(userId: String): Flow<List<EntidadEntrenamiento>>

    @Query("SELECT * FROM entrenamientos WHERE userId = :userId")
    suspend fun obtenerPorUsuarioSync(userId: String): List<EntidadEntrenamiento>

    @Query("SELECT * FROM entrenamientos WHERE userId = :userId LIMIT 1")
    suspend fun obtenerEntrenamientoDeHoy(userId: String): EntidadEntrenamiento?

    @Query("SELECT * FROM entrenamientos WHERE userId = :userId AND fecha BETWEEN :inicio AND :fin ORDER BY fecha ASC")
    fun observarEntreFechas(userId: String, inicio: Long, fin: Long): Flow<List<EntidadEntrenamiento>>

    @Query("SELECT * FROM entrenamientos WHERE userId = :userId AND fecha = :fecha LIMIT 1")
    suspend fun obtenerEnFecha(userId: String, fecha: Long): EntidadEntrenamiento?

    @Query("SELECT * FROM entrenamientos WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EntidadEntrenamiento>

    @Query("UPDATE entrenamientos SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)

    @Query(
        "UPDATE entrenamientos SET ejerciciosRealizados = :ejerciciosRealizados, " +
            "completo = (:ejerciciosRealizados >= totalEjercicios) WHERE id = :id"
    )
    suspend fun actualizarProgreso(id: String, ejerciciosRealizados: Int)
}
