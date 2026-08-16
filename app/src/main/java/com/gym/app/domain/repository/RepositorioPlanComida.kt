/**
 * @file RepositorioPlanComida.kt
 * @brief Puerto de repositorio del plan de comidas diario en la capa de dominio.
 * Define el contrato para observar y persistir los planes de dieta del método
 * Naturvitia (importados desde PDF del nutricionista o creados manualmente).
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.PlanComida
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * @interface RepositorioPlanComida
 * @brief Contrato de acceso a los planes de comidas del usuario.
 */
interface RepositorioPlanComida {

    /**
     * @brief Observa de forma reactiva el plan de comidas de una fecha concreta.
     * @param fecha Fecha de la que se desea observar el plan.
     * @return Flujo reactivo con el [PlanComida] de esa fecha, o `null` si no existe.
     */
    fun observarPlanDeHoy(fecha: LocalDate): Flow<PlanComida?>

    /**
     * @brief Observa de forma reactiva los planes de comidas dentro de un rango de fechas.
     * @param inicio Fecha inicial del rango (incluida).
     * @param fin Fecha final del rango (incluida).
     * @return Flujo reactivo con la lista de [PlanComida] comprendidos en el rango.
     */
    fun observarPlanesEntre(inicio: LocalDate, fin: LocalDate): Flow<List<PlanComida>>

    /**
     * @brief Guarda o actualiza un plan de comidas completo.
     * @param plan Plan de comidas a persistir.
     */
    suspend fun guardarPlan(plan: PlanComida)

    /**
     * @brief Reemplaza por completo el plan de comidas de un día.
     * Operación transaccional utilizada por el módulo de rebalanceo intra-día para
     * sustituir las tomas del día por las versiones ajustadas.
     * @param plan Nuevo plan que reemplaza al existente para su fecha.
     */
    suspend fun reemplazarPlanDelDia(plan: PlanComida)
}