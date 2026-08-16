/**
 * @file DaoRutina.kt
 * @brief DAO para la gestión de rutinas de entrenamiento y sus bloques.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadBloqueRutina
import com.gym.app.data.local.entidad.EntidadRutina
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoRutina
 * @brief Operaciones de base de datos para las rutinas de entrenamiento.
 */
@Dao
interface DaoRutina {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRutina(rutina: EntidadRutina)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarBloque(bloque: EntidadBloqueRutina)

    @Query("SELECT * FROM rutina WHERE userId = :userId")
    fun observarRutinas(userId: String): Flow<List<EntidadRutina>>

    @Query("DELETE FROM rutina WHERE id = :id")
    suspend fun eliminarRutina(id: String)

    @Query("DELETE FROM bloque_rutina WHERE rutinaId = :rutinaId")
    suspend fun eliminarBloquesDeRutina(rutinaId: String)

    @Query("SELECT * FROM bloque_rutina WHERE rutinaId = :rutinaId ORDER BY serie ASC")
    suspend fun obtenerBloquesDeRutina(rutinaId: String): List<EntidadBloqueRutina>
}