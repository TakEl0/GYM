/**
 * @file DaoUsuarioPerfil.kt
 * @brief DAO para la gestión del perfil de usuario en Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadUsuarioPerfil
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoUsuarioPerfil
 * @brief Operaciones de base de datos para perfiles de usuario.
 */
@Dao
interface DaoUsuarioPerfil {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPerfil(perfil: EntidadUsuarioPerfil)

    @Query("SELECT * FROM perfiles WHERE id = :id LIMIT 1")
    fun observarPerfil(id: String): Flow<EntidadUsuarioPerfil?>

    @Query("SELECT * FROM perfiles WHERE id = :id LIMIT 1")
    suspend fun obtenerPerfilPorId(id: String): EntidadUsuarioPerfil?
}
