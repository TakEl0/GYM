/**
 * @file DaoPerfilUsuario.kt
 * @brief DAO para la gestión del perfil del usuario en Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadPerfilUsuario
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoPerfilUsuario
 * @brief Operaciones de base de datos para el perfil y objetivos nutricionales.
 */
@Dao
interface DaoPerfilUsuario {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(perfil: EntidadPerfilUsuario)

    @Query("SELECT * FROM perfil_usuario WHERE id = :id")
    fun observarPerfil(id: String): Flow<EntidadPerfilUsuario?>

    @Query("SELECT * FROM perfil_usuario WHERE id = :id")
    suspend fun obtenerPerfil(id: String): EntidadPerfilUsuario?

    @Query(
        """
        UPDATE perfil_usuario SET
            pesoObjetivoKg = :pesoObjetivoKg,
            alturaCm = :alturaCm,
            edad = :edad,
            sexo = :sexo,
            factorActividad = :factorActividad,
            objetivo = :objetivo
        WHERE id = :id
        """
    )
    suspend fun actualizarObjetivos(
        id: String,
        pesoObjetivoKg: Double?,
        alturaCm: Double?,
        edad: Int?,
        sexo: String?,
        factorActividad: String?,
        objetivo: String?
    )
}