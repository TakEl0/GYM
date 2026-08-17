/**
 * @file DaoMapeoAprendido.kt
 * @brief DAO para la gestión de los mapeos aprendidos (correcciones manuales) en Room.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadMapeoAprendido
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoMapeoAprendido
 * @brief Operaciones de base de datos sobre la tabla `mapeos_aprendidos`.
 */
@Dao
interface DaoMapeoAprendido {

    /**
     * @brief Inserta o reemplaza un mapeo aprendido por su nombre normalizado.
     * @param entidad Mapeo aprendido a persistir.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entidad: EntidadMapeoAprendido)

    /**
     * @brief Busca un mapeo aprendido por su clave (nombre de ejercicio normalizado).
     * @param nombreNormalizado Clave de búsqueda.
     * @return [EntidadMapeoAprendido] si existe, o `null` si no se encontró.
     */
    @Query("SELECT * FROM mapeos_aprendidos WHERE nombreNormalizado = :nombreNormalizado")
    suspend fun buscar(nombreNormalizado: String): EntidadMapeoAprendido?

    /**
     * @brief Observa de forma reactiva todos los mapeos aprendidos.
     * @return Flujo reactivo con la lista completa de mapeos aprendidos.
     */
    @Query("SELECT * FROM mapeos_aprendidos")
    fun observarTodos(): Flow<List<EntidadMapeoAprendido>>
}