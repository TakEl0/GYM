/**
 * @file DaoPlanComida.kt
 * @brief DAO para la gestión de planes de comidas y sus tomas e ingredientes.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadIngredienteToma
import com.gym.app.data.local.entidad.EntidadPlanComida
import com.gym.app.data.local.entidad.EntidadToma
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoPlanComida
 * @brief Operaciones de base de datos para los planes de comidas diarios.
 */
@Dao
interface DaoPlanComida {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPlan(plan: EntidadPlanComida)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarToma(toma: EntidadToma)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarIngrediente(ingrediente: EntidadIngredienteToma)

    @Query(
        "SELECT * FROM plan_comida WHERE userId = :userId AND fecha >= :inicioDia AND fecha <= :finDia"
    )
    fun observarPlanDeHoy(userId: String, inicioDia: Long, finDia: Long): Flow<EntidadPlanComida?>

    @Query("SELECT * FROM plan_comida WHERE userId = :userId AND fecha >= :inicio AND fecha <= :fin")
    fun observarPlanesEntre(userId: String, inicio: Long, fin: Long): Flow<List<EntidadPlanComida>>

    @Query("DELETE FROM plan_comida WHERE id = :id")
    suspend fun eliminarPlan(id: String)

    @Query("DELETE FROM toma WHERE planComidaId = :planId")
    suspend fun eliminarTomasDePlan(planId: String)

    @Query("DELETE FROM ingrediente_toma WHERE tomaId = :tomaId")
    suspend fun eliminarIngredientesDeToma(tomaId: String)

    @Query("SELECT * FROM toma WHERE planComidaId = :planId ORDER BY orden ASC")
    suspend fun obtenerTomasDePlan(planId: String): List<EntidadToma>

    @Query("SELECT * FROM ingrediente_toma WHERE tomaId = :tomaId")
    suspend fun obtenerIngredientesDeToma(tomaId: String): List<EntidadIngredienteToma>
}