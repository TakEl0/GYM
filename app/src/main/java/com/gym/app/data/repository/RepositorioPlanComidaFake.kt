/**
 * @file RepositorioPlanComidaFake.kt
 * @brief Implementación simulada del repositorio de planes de comidas.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.repository.RepositorioPlanComida
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * @class RepositorioPlanComidaFake
 * @brief Repositorio de planes de comidas en memoria para desarrollo y tests.
 */
class RepositorioPlanComidaFake : RepositorioPlanComida {

    private val planes = MutableStateFlow<List<PlanComida>>(emptyList())

    override fun observarPlanDeHoy(fecha: LocalDate): Flow<PlanComida?> =
        planes.map { lista -> lista.firstOrNull { it.fecha == fecha } }

    override fun observarPlanesEntre(inicio: LocalDate, fin: LocalDate): Flow<List<PlanComida>> =
        planes.map { lista -> lista.filter { !it.fecha.isBefore(inicio) && !it.fecha.isAfter(fin) } }

    override suspend fun guardarPlan(plan: PlanComida) {
        planes.value = planes.value + plan
    }

    override suspend fun reemplazarPlanDelDia(plan: PlanComida) {
        planes.value = planes.value.filterNot { it.fecha == plan.fecha } + plan
    }
}