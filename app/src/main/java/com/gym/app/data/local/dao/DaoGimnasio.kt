/**
 * @file DaoGimnasio.kt
 * @brief DAO para la gestión del gimnasio del usuario y sus máquinas.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadGimnasio
import com.gym.app.data.local.entidad.EntidadMaquina
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoGimnasio
 * @brief Operaciones de base de datos para el gimnasio y su maquinaria.
 */
@Dao
interface DaoGimnasio {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGimnasio(gimnasio: EntidadGimnasio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMaquina(maquina: EntidadMaquina)

    @Query("SELECT * FROM gimnasio WHERE userId = :userId")
    fun observarGimnasio(userId: String): Flow<EntidadGimnasio?>

    @Query("DELETE FROM maquina WHERE gimnasioId = :gimnasioId")
    suspend fun eliminarMaquinasDeGimnasio(gimnasioId: String)

    @Query("SELECT * FROM maquina WHERE gimnasioId = :gimnasioId")
    suspend fun obtenerMaquinasDeGimnasio(gimnasioId: String): List<EntidadMaquina>

    @Query("UPDATE maquina SET disponible = :disponible WHERE id = :maquinaId")
    suspend fun actualizarDisponibilidadMaquina(maquinaId: String, disponible: Boolean)
}