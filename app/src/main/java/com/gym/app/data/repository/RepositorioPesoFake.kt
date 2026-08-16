/**
 * @file RepositorioPesoFake.kt
 * @brief Implementación simulada del repositorio de peso corporal.
 * Mantiene en memoria un historial de ejemplo para desarrollar y validar la
 * pantalla de registro de peso sin depender de fuentes reales (Room,
 * Health Connect o Supabase).
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import java.time.LocalDate

/**
 * @class RepositorioPesoFake
 * @brief Implementación en memoria del repositorio de peso.
 * Almacena los registros en una lista mutable y permite guardar nuevos
 * registros durante la sesión de desarrollo.
 */
class RepositorioPesoFake : RepositorioPeso {

    private val registros = mutableListOf(
        RegistroPeso(
            fecha = LocalDate.now().minusDays(7),
            pesoKg = 82.4,
            grasaCorporalPorcentaje = 18.2,
            masaMuscularKg = 34.6
        ),
        RegistroPeso(
            fecha = LocalDate.now().minusDays(14),
            pesoKg = 83.1,
            grasaCorporalPorcentaje = 18.9,
            masaMuscularKg = 34.1
        ),
        RegistroPeso(
            fecha = LocalDate.now().minusDays(21),
            pesoKg = 83.9,
            grasaCorporalPorcentaje = 19.4,
            masaMuscularKg = 33.8
        )
    )

    override suspend fun obtenerHistorial(): List<RegistroPeso> =
        registros.sortedByDescending { it.fecha }

    override suspend fun guardarRegistro(registro: RegistroPeso) {
        registros.removeAll { it.fecha == registro.fecha }
        registros.add(registro)
    }

    override suspend fun obtenerUltimoRegistro(): RegistroPeso? =
        registros.maxByOrNull { it.fecha }

    override suspend fun obtenerPesoEnFecha(fecha: LocalDate): RegistroPeso? =
        registros.firstOrNull { it.fecha == fecha }
}