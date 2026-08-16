/**
 * @file RepositorioPlanComidaRoom.kt
 * @brief Implementación del repositorio de planes de comidas con Room local.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.repository.RepositorioPlanComida
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioPlanComidaRoom
 * @brief Administra los planes de comidas diarios generados desde la dieta
 * importada. Los planes son locales por ahora (no se sincronizan con Supabase);
 * la sincronización remota se añadirá cuando el esquema remoto lo soporte.
 */
class RepositorioPlanComidaRoom(private val context: Context) : RepositorioPlanComida {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    private fun obtenerUserIdActual(): String {
        return try {
            val supabase = com.gym.app.data.remote.ClienteSupabase.inicializar(context)
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarPlanDeHoy(fecha: LocalDate): Flow<PlanComida?> {
        val userId = obtenerUserIdActual()
        val inicioDia = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finDia = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return db.daoPlanComida().observarPlanDeHoy(userId, inicioDia, finDia)
            .map { entidad ->
                entidad?.let { plan ->
                    val tomas = db.daoPlanComida().obtenerTomasDePlan(plan.id)
                    val ingredientes = tomas.associate { toma ->
                        toma.id to db.daoPlanComida().obtenerIngredientesDeToma(toma.id)
                    }
                    plan.aDominio(tomas, ingredientes)
                }
            }
    }

    override fun observarPlanesEntre(inicio: LocalDate, fin: LocalDate): Flow<List<PlanComida>> {
        val userId = obtenerUserIdActual()
        val inicioEpoch = inicio.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finEpoch = fin.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return db.daoPlanComida().observarPlanesEntre(userId, inicioEpoch, finEpoch)
            .map { planes ->
                planes.map { plan ->
                    val tomas = db.daoPlanComida().obtenerTomasDePlan(plan.id)
                    val ingredientes = tomas.associate { toma ->
                        toma.id to db.daoPlanComida().obtenerIngredientesDeToma(toma.id)
                    }
                    plan.aDominio(tomas, ingredientes)
                }
            }
    }

    override suspend fun guardarPlan(plan: PlanComida) {
        val userId = obtenerUserIdActual()
        db.daoPlanComida().insertarPlan(plan.aEntidad(userId))
        for (toma in plan.tomas) {
            db.daoPlanComida().insertarToma(toma.aEntidad(plan.id))
            for (ingrediente in toma.ingredientes) {
                db.daoPlanComida().insertarIngrediente(ingrediente.aEntidad(toma.id))
            }
        }
    }

    override suspend fun reemplazarPlanDelDia(plan: PlanComida) {
        val userId = obtenerUserIdActual()
        // Elimina el plan existente del mismo día para reemplazarlo por completo.
        val inicioDia = plan.fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finDia = plan.fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val existente = db.daoPlanComida().observarPlanDeHoy(userId, inicioDia, finDia).first()

        if (existente != null) {
            val tomas = db.daoPlanComida().obtenerTomasDePlan(existente.id)
            tomas.forEach { db.daoPlanComida().eliminarIngredientesDeToma(it.id) }
            db.daoPlanComida().eliminarTomasDePlan(existente.id)
            db.daoPlanComida().eliminarPlan(existente.id)
        }

        guardarPlan(plan)
    }
}