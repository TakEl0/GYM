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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * @class RepositorioEntrenamientoFake
 * @brief Implementación en memoria del repositorio de entrenamientos.
 * Devuelve una rutina de ejemplo para hoy y contadores semanales simulados.
 */
class RepositorioEntrenamientoFake : RepositorioEntrenamiento {

    private var entrenamientoDeHoy = Entrenamiento(
        id = "rutina-push-a",
        nombre = "Push A - Pecho y Tríceps",
        grupoMuscular = listOf("Pecho", "Hombro", "Tríceps"),
        seriesTotales = 18,
        ejerciciosRealizados = 4,
        totalEjercicios = 6,
        duracionMinutos = 48,
        completo = false,
        fecha = System.currentTimeMillis()
    )

    private val semanaCompleta = listOf(
        entrenamientoDeHoy,
        Entrenamiento(
            id = "rutina-pull-a",
            nombre = "Pull A - Espalda y Bíceps",
            grupoMuscular = listOf("Espalda", "Bíceps"),
            seriesTotales = 16,
            ejerciciosRealizados = 6,
            totalEjercicios = 6,
            duracionMinutos = 45,
            completo = true,
            fecha = System.currentTimeMillis() - 86_400_000L
        ),
        Entrenamiento(
            id = "rutina-legs-a",
            nombre = "Legs A - Pierna y Glúteo",
            grupoMuscular = listOf("Cuádriceps", "Isquios", "Glúteo"),
            seriesTotales = 20,
            ejerciciosRealizados = 5,
            totalEjercicios = 7,
            duracionMinutos = 55,
            completo = false,
            fecha = System.currentTimeMillis() + 86_400_000L
        )
    )

    override fun observarEntrenamientos(userId: String?): Flow<List<Entrenamiento>> =
        flow { emit(semanaCompleta) }

    override suspend fun obtenerEntrenamientoDeHoy(): Entrenamiento = entrenamientoDeHoy

    override fun observarEntrenamientosEntre(inicio: Long, fin: Long): Flow<List<Entrenamiento>> =
        flow { emit(semanaCompleta.filter { it.fecha in inicio..fin }) }

    override suspend fun obtenerEntrenamientoEnFecha(fecha: Long): Entrenamiento? =
        semanaCompleta.firstOrNull { it.fecha == fecha }

    override suspend fun obtenerSesionesCompletadasSemana(): Int = 4

    override suspend fun obtenerTotalSesionesSemana(): Int = 6

    override suspend fun actualizarProgreso(entrenamientoId: String, ejerciciosRealizados: Int) {
        if (entrenamientoDeHoy.id == entrenamientoId) {
            entrenamientoDeHoy = entrenamientoDeHoy.copy(
                ejerciciosRealizados = ejerciciosRealizados,
                completo = ejerciciosRealizados >= entrenamientoDeHoy.totalEjercicios
            )
        }
    }

    override suspend fun guardarEntrenamiento(entrenamiento: Entrenamiento) {
        // En Fake, actualizamos o añadimos a la lista simulada
        entrenamientoDeHoy = entrenamiento
    }
}