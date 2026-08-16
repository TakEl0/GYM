/**
 * @file RepositorioComidaFake.kt
 * @brief Implementación simulada del repositorio de comidas.
 * Permite desarrollar y validar la pantalla de nutrición con datos de ejemplo
 * en memoria cuando Supabase no está configurado (modo desarrollo).
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.Comida
import com.gym.app.domain.repository.RepositorioComida
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

/**
 * @class RepositorioComidaFake
 * @brief Implementación en memoria del repositorio de comidas.
 * Mantiene una lista mutable de ingestas de ejemplo y permite registrar,
 * eliminar y observar las comidas de una fecha concreta.
 */
class RepositorioComidaFake : RepositorioComida {

    private val comidas = mutableListOf(
        Comida(
            id = "comida-1",
            nombre = "Desayuno Naturvitia: Tortilla de claras y avena",
            kcal = 420,
            proteinasG = 32.0,
            carbohidratosG = 48.0,
            grasasG = 9.0,
            tipoIngesta = "DESAYUNO",
            fecha = LocalDate.now()
        ),
        Comida(
            id = "comida-2",
            nombre = "Comida: Pollo a la plancha con arroz integral",
            kcal = 580,
            proteinasG = 48.0,
            carbohidratosG = 62.0,
            grasasG = 12.0,
            tipoIngesta = "COMIDA",
            fecha = LocalDate.now()
        ),
        Comida(
            id = "comida-3",
            nombre = "Merienda: Batido de proteína y frutos secos",
            kcal = 340,
            proteinasG = 28.0,
            carbohidratosG = 22.0,
            grasasG = 14.0,
            tipoIngesta = "MERIENDA",
            fecha = LocalDate.now()
        )
    )

    override fun observarComidasPorFecha(fecha: LocalDate): Flow<List<Comida>> =
        flow { emit(comidas.filter { it.fecha == fecha }) }

    override suspend fun guardarComida(comida: Comida) {
        comidas.removeAll { it.id == comida.id }
        comidas.add(comida)
    }

    override suspend fun eliminarComida(id: String) {
        comidas.removeAll { it.id == id }
    }

    override suspend fun sincronizarConRemoto(): Result<Unit> = Result.success(Unit)
}