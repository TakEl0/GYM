/**
 * @file DaoRegistroPeso.kt
 * @brief DAO para la gestión de registros de peso corporal en Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadRegistroPeso
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoRegistroPeso
 * @brief Operaciones de base de datos para registros de peso.
 */
@Dao
interface DaoRegistroPeso {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(registro: EntidadRegistroPeso)

    @Query("SELECT * FROM registros_peso WHERE userId = :userId ORDER BY fecha DESC")
    fun observarPorUsuario(userId: String): Flow<List<EntidadRegistroPeso>>

    @Query("SELECT * FROM registros_peso WHERE userId = :userId ORDER BY fecha DESC")
    suspend fun obtenerPorUsuarioSync(userId: String): List<EntidadRegistroPeso>

    @Query("SELECT * FROM registros_peso WHERE userId = :userId ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerUltimo(userId: String): EntidadRegistroPeso?

    @Query("SELECT * FROM registros_peso WHERE userId = :userId AND fecha = :fecha LIMIT 1")
    suspend fun obtenerEnFecha(userId: String, fecha: Long): EntidadRegistroPeso?

    @Query("SELECT * FROM registros_peso WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EntidadRegistroPeso>

    @Query("UPDATE registros_peso SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)
}
