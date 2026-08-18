/**
 * @file DaoSerieRealizada.kt
 * @brief DAO para la gestión de series realizadas en la base de datos local Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadSerieRealizada
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoSerieRealizada
 * @brief Operaciones de base de datos para el almacenamiento y consulta de series realizadas.
 */
@Dao
interface DaoSerieRealizada {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(serie: EntidadSerieRealizada)

    @Query("SELECT * FROM serie_realizada WHERE sesionId = :sesionId ORDER BY numeroSerie ASC")
    fun observarPorSesion(sesionId: String): Flow<List<EntidadSerieRealizada>>

    @Query("SELECT * FROM serie_realizada WHERE id = :id")
    suspend fun obtenerPorId(id: String): EntidadSerieRealizada?

    @Query("DELETE FROM serie_realizada WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("SELECT pesoKg FROM serie_realizada WHERE ejercicioId = :ejercicioId ORDER BY fecha DESC LIMIT 1")
    suspend fun ultimoPesoPorEjercicio(ejercicioId: String): Double?

    @Query("SELECT * FROM serie_realizada WHERE sincronizado = 0")
    suspend fun obtenerPendientesSincronizar(): List<EntidadSerieRealizada>

    @Query("UPDATE serie_realizada SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)
}
