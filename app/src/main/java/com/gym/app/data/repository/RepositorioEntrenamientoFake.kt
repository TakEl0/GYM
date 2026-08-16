/**
 * @file RepositorioEntrenamientoFake.kt
 * @brief Implementación simulada del repositorio de entrenamientos.
 * Proporciona datos de ejemplo para desarrollar y validar la interfaz de
 * usuario sin depender de la base de datos Room ni de la API. En un futuro,
 * esta implementación será sustituida por una basada en fuentes reales.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.repository.RepositorioEntrenamiento

/**
 * @class RepositorioEntrenamientoFake
 * @brief Implementación en memoria del repositorio de entrenamientos.
 * Devuelve una rutina de ejemplo para hoy y contadores semanales simulados.
 */
class RepositorioEntrenamientoFake : RepositorioEntrenamiento {

    override suspend fun obtenerEntrenamientoDeHoy(): Entrenamiento = Entrenamiento(
        id = "rutina-push-a",
        nombre = "Push A - Pecho y Tríceps",
        grupoMuscular = listOf("Pecho", "Hombro", "Tríceps"),
        seriesTotales = 18,
        ejerciciosRelizados = 4,
        totalEjercicios = 6,
        duracionMinutos = 48,
        completo = false
    )

    override suspend fun obtenerSesionesCompletadasSemana(): Int = 4

    override suspend fun obtenerTotalSesionesSemana(): Int = 6
}