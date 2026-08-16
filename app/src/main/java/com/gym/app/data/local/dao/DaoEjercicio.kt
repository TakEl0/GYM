/**
 * @file DaoEjercicio.kt
 * @brief DAO para la gestión del catálogo de ejercicios del usuario.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadEjercicio
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoEjercicio
 * @brief Operaciones de base de datos para el catálogo de ejercicios.
 */
@Dao
interface DaoEjercicio {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ejercicio: EntidadEjercicio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(ejercicios: List<EntidadEjercicio>)

    @Query("SELECT * FROM ejercicio WHERE userId = :userId")
    fun observarEjercicios(userId: String): Flow<List<EntidadEjercicio>>
}