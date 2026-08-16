/**
 * @file DaoComida.kt
 * @brief DAO para la gestión de comidas e ingestas nutricionales en Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadComida
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoComida
 * @brief Operaciones de base de datos para los registros nutricionales.
 */
@Dao
interface DaoComida {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(comida: EntidadComida)

    @Query("SELECT * FROM comidas WHERE userId = :userId AND fecha >= :inicioDia AND fecha <= :finDia")
    fun observarComidasPorFecha(userId: String, inicioDia: Long, finDia: Long): Flow<List<EntidadComida>>

    @Query("DELETE FROM comidas WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("SELECT * FROM comidas WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EntidadComida>

    @Query("UPDATE comidas SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)
}
