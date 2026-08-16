/**
 * @file RepositorioSesionEntrenamientoFake.kt
 * @brief Implementación simulada del repositorio de sesiones de entrenamiento.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioSesionEntrenamientoFake
 * @brief Repositorio de sesiones de entrenamiento en memoria para tests y desarrollo.
 */
class RepositorioSesionEntrenamientoFake : RepositorioSesionEntrenamiento {

    private val sesiones = MutableStateFlow<List<SesionEntrenamiento>>(emptyList())

    override fun observarSesiones(inicio: LocalDate, fin: LocalDate): Flow<List<SesionEntrenamiento>> =
        sesiones.map { lista ->
            lista.filter { sesion ->
                val fechaSesion = LocalDate.ofEpochDay(
                    java.time.Instant.ofEpochMilli(sesion.fecha)
                        .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                )
                !fechaSesion.isBefore(inicio) && !fechaSesion.isAfter(fin)
            }
        }

    override suspend fun guardarSesion(sesion: SesionEntrenamiento) {
        sesiones.value = sesiones.value + sesion
    }

    override suspend fun sesionesCompletadasSemana(fecha: LocalDate): Int {
        val inicioSemana = fecha.minusDays((fecha.dayOfWeek.value - 1).toLong())
        val finSemana = fecha.plusDays((7 - fecha.dayOfWeek.value).toLong())
        return sesiones.value.count { sesion ->
            val fechaSesion = java.time.Instant.ofEpochMilli(sesion.fecha)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            sesion.completo && !fechaSesion.isBefore(inicioSemana) && !fechaSesion.isAfter(finSemana)
        }
    }
}