/**
 * @file DaoIngesta.kt
 * @brief DAO para la gestión de ingestas registradas (comidas consumidas).
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadIngestaRegistrada
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoIngesta
 * @brief Operaciones de base de datos para las ingestas realmente consumidas.
 */
@Dao
interface DaoIngesta {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ingesta: EntidadIngestaRegistrada)

    @Query("SELECT * FROM ingesta_registrada WHERE userId = :userId AND fecha >= :inicioDia AND fecha <= :finDia")
    fun observarIngestasDelDia(userId: String, inicioDia: Long, finDia: Long): Flow<List<EntidadIngestaRegistrada>>

    @Query("DELETE FROM ingesta_registrada WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("SELECT * FROM ingesta_registrada WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EntidadIngestaRegistrada>

    @Query("UPDATE ingesta_registrada SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)
}