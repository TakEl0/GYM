/**
 * @file ConstruirRutinaCasoUso.kt
 * @brief Caso de uso de construcción automática de la rutina PPL del día.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.usecase.gimnasio.AlternativasMaquinaCasoUso
import java.util.UUID

/**
 * @class ConstruirRutinaCasoUso
 * @brief Construye automáticamente la rutina de entrenamiento del día siguiendo
 * la programación PPL del método Naturvitia:
 *
 * - Lunes (1) y Jueves (4) → **Pecho** (Push).
 * - Martes (2) y Viernes (5) → **Espalda** (Pull).
 * - Miércoles (3) y Sábado (6) → **Pierna**.
 * - Domingo (7) → descanso (se devuelve `null`).
 *
 * Selecciona los ejercicios del catálogo cuyo grupo muscular principal coincide
 * con el grupo del día y cuya máquina asociada (si el ejercicio requiere una)
 * está disponible en el gimnasio. Si un ejercicio principal requiere una máquina
 * no disponible, se busca un sustituto mediante [AlternativasMaquinaCasoUso].
 *
 * Si tras el proceso no se alcanzan al menos 3 ejercicios, se devuelve `null`
 * (el día no puede programarse con garantías). Cada ejercicio seleccionado genera
 * 4 bloques de serie (serie 1 a 4) con 10 repeticiones y 90 segundos de descanso.
 */
class ConstruirRutinaCasoUso(
    private val alternativasMaquinaCasoUso: AlternativasMaquinaCasoUso
) {

    /**
     * @brief Construye la rutina PPL del día solicitado.
     * @param diaSemana Día de la semana (1 = lunes ... 7 = domingo).
     * @param maquinasDisponibles Máquinas operativas y libres del gimnasio.
     * @param ejercicios Catálogo de ejercicios disponibles.
     * @return [Result] con la [Rutina] construida, `null` si el día es de descanso
     * o no se alcanzan 3 ejercicios válidos, o con el error producido.
     */
    suspend fun ejecutar(
        diaSemana: Int,
        maquinasDisponibles: List<Maquina>,
        ejercicios: List<Ejercicio>
    ): Result<Rutina?> {
        val grupoDia = grupoMuscularDelDia(diaSemana)
            ?: return Result.success(null)

        val ejerciciosDelGrupo = ejercicios.filter { ejercicio ->
            ejercicio.grupoMuscularPrincipal.equals(grupoDia, ignoreCase = true)
        }
        if (ejerciciosDelGrupo.isEmpty()) return Result.success(null)

        val idsMaquinasDisponibles = maquinasDisponibles
            .filter { it.disponible }
            .map { it.id }
            .toSet()

        // Selección de ejercicios, sustituyendo los que requieren máquina no disponible.
        val ejerciciosSeleccionados = mutableListOf<Ejercicio>()
        for (ejercicio in ejerciciosDelGrupo) {
            val requiereMaquina = ejercicio.maquinaId != null
            val maquinaDisponible = !requiereMaquina || ejercicio.maquinaId in idsMaquinasDisponibles

            if (maquinaDisponible) {
                ejerciciosSeleccionados.add(ejercicio)
                continue
            }

            // La máquina no está disponible: se busca un sustituto en el catálogo.
            val sustitutos = alternativasMaquinaCasoUso
                .ejecutar(ejercicio.maquinaId!!, ejercicios)
                .getOrNull()
                .orEmpty()
            val sustituto = sustitutos.firstOrNull { candidato ->
                ejerciciosSeleccionados.none { it.id == candidato.id }
            }
            if (sustituto != null) {
                ejerciciosSeleccionados.add(sustituto)
            }
        }

        if (ejerciciosSeleccionados.size < MINIMO_EJERCICIOS) return Result.success(null)

        val bloques = ejerciciosSeleccionados.flatMap { ejercicio ->
            (1..SERIES_POR_EJERCICIO).map { serie ->
                BloqueRutina(
                    id = UUID.randomUUID().toString(),
                    ejercicioId = ejercicio.id,
                    serie = serie,
                    repeticiones = REPETICIONES_POR_SERIE,
                    pesoKg = null,
                    descansoSegundos = DESCANSO_SEGUNDOS
                )
            }
        }

        val rutina = Rutina(
            id = UUID.randomUUID().toString(),
            nombre = "PPL - $grupoDia",
            descripcion = "Rutina PPL construida automáticamente para el día ${nombreDia(diaSemana)}.",
            diasSemana = listOf(diaSemana),
            bloques = bloques
        )
        return Result.success(rutina)
    }

    /**
     * @brief Resuelve el grupo muscular principal según el día de la semana PPL.
     * @param diaSemana Día de la semana (1 = lunes ... 7 = domingo).
     * @return Grupo muscular del día, o `null` si el día es de descanso (domingo)
     * o el valor no está en el rango válido.
     */
    private fun grupoMuscularDelDia(diaSemana: Int): String? = when (diaSemana) {
        Rutina.LUNES, Rutina.JUEVES -> GRUPO_PECHO
        Rutina.MARTES, Rutina.VIERNES -> GRUPO_ESPALDA
        Rutina.MIERCOLES, Rutina.SABADO -> GRUPO_PIERNA
        else -> null
    }

    /**
     * @brief Devuelve el nombre legible del día de la semana.
     * @param diaSemana Día de la semana (1 = lunes ... 7 = domingo).
     * @return Nombre del día en minúsculas (p. ej. "lunes").
     */
    private fun nombreDia(diaSemana: Int): String = when (diaSemana) {
        Rutina.LUNES -> "lunes"
        Rutina.MARTES -> "martes"
        Rutina.MIERCOLES -> "miércoles"
        Rutina.JUEVES -> "jueves"
        Rutina.VIERNES -> "viernes"
        Rutina.SABADO -> "sábado"
        else -> "domingo"
    }

    companion object {
        /** Grupo muscular de los días de empuje (Push). */
        private const val GRUPO_PECHO: String = "Pecho"

        /** Grupo muscular de los días de tracción (Pull). */
        private const val GRUPO_ESPALDA: String = "Espalda"

        /** Grupo muscular de los días de pierna. */
        private const val GRUPO_PIERNA: String = "Pierna"

        /** Número mínimo de ejercicios para poder construir la rutina. */
        private const val MINIMO_EJERCICIOS: Int = 3

        /** Series prescritas por ejercicio (1..4). */
        private const val SERIES_POR_EJERCICIO: Int = 4

        /** Repeticiones por serie prescritas en cada bloque. */
        private const val REPETICIONES_POR_SERIE: Int = 10

        /** Descanso entre series en segundos. */
        private const val DESCANSO_SEGUNDOS: Int = 90
    }
}