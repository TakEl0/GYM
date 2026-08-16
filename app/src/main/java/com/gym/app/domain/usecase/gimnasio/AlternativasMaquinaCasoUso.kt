/**
 * @file AlternativasMaquinaCasoUso.kt
 * @brief Caso de uso del motor de sustituciones de maquinaria del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioGimnasio
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * @class AlternativasMaquinaCasoUso
 * @brief Sugiere ejercicios alternativos cuando una máquina del gimnasio no está
 * disponible (en reparación u ocupada), aplicando la tabla de sustitución del
 * método Naturvitia según el **nombre** de la máquina:
 *
 * - Máquinas cuyo nombre contiene **PRENSA** → ejercicios con "sentadilla" en el
 *   nombre (sentadilla Hack o búlgara).
 * - Máquinas cuyo nombre contiene **JALÓN** o **JALON** → ejercicios de dominadas
 *   o remo (dominada asistida, remo en polea alta).
 * - Máquinas cuyo nombre contiene **EXTENSIÓN** o **EXTENSION** → ejercicios de
 *   zancadas o sentadilla sissy.
 * - Cualquier otra máquina → ejercicios cuyo grupo muscular coincide con los
 *   grupos musculares que trabaja la máquina.
 *
 * El emparejamiento es insensible a mayúsculas y usa coincidencia parcial. Para
 * localizar la máquina por su identificador se consulta el gimnasio del usuario
 * mediante [RepositorioGimnasio.observarGimnasio].
 */
class AlternativasMaquinaCasoUso(
    private val repositorioGimnasio: RepositorioGimnasio,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Busca ejercicios alternativos para la máquina indicada.
     * @param maquinaId Identificador de la máquina no disponible.
     * @param ejerciciosDisponibles Catálogo de ejercicios donde buscar sustitutos.
     * @return [Result] con la lista de [Ejercicio] alternativos (vacía si no se
     * encuentra la máquina o no hay sustitutos), o con el error de consulta.
     */
    suspend fun ejecutar(
        maquinaId: String,
        ejerciciosDisponibles: List<Ejercicio>
    ): Result<List<Ejercicio>> = withContext(dispatcher) {
        try {
            val gimnasio: Gimnasio? = repositorioGimnasio.observarGimnasio().first()
            val maquina = gimnasio?.maquinas?.firstOrNull { it.id == maquinaId }
                ?: return@withContext Result.success(emptyList())

            val sustitutos = buscarSustitutos(maquina, ejerciciosDisponibles)
            Result.success(sustitutos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * @brief Aplica la tabla de sustitución por nombre de máquina.
     * @param maquina Máquina que se desea sustituir.
     * @param ejerciciosDisponibles Catálogo de ejercicios candidatos.
     * @return Lista de [Ejercicio] alternativos según la tabla de sustitución.
     */
    private fun buscarSustitutos(
        maquina: Maquina,
        ejerciciosDisponibles: List<Ejercicio>
    ): List<Ejercicio> {
        val nombre = maquina.nombre
        return when {
            nombre.contains(CLAVE_PRENSA, ignoreCase = true) ->
                ejerciciosDisponibles.filter { ejercicio ->
                    ejercicio.nombre.contains(PALABRA_SENTADILLA, ignoreCase = true)
                }

            nombre.contains(CLAVE_JALON_ACENTUADA, ignoreCase = true) ||
                nombre.contains(CLAVE_JALON_SIN_ACENTO, ignoreCase = true) ->
                ejerciciosDisponibles.filter { ejercicio ->
                    ejercicio.nombre.contains(PALABRA_DOMINADA, ignoreCase = true) ||
                        ejercicio.nombre.contains(PALABRA_REMO, ignoreCase = true)
                }

            nombre.contains(CLAVE_EXTENSION_ACENTUADA, ignoreCase = true) ||
                nombre.contains(CLAVE_EXTENSION_SIN_ACENTO, ignoreCase = true) ->
                ejerciciosDisponibles.filter { ejercicio ->
                    ejercicio.nombre.contains(PALABRA_ZANCADA, ignoreCase = true) ||
                        ejercicio.nombre.contains(PALABRA_SENTADILLA_SISSY, ignoreCase = true)
                }

            // Caso por defecto: mismo grupo muscular que trabaja la máquina.
            else -> ejerciciosDisponibles.filter { ejercicio ->
                maquina.grupoMuscular.any { grupo ->
                    ejercicio.grupoMuscularPrincipal.equals(grupo, ignoreCase = true) ||
                        ejercicio.grupoMuscularSecundario?.equals(grupo, ignoreCase = true) == true
                }
            }
        }
    }

    companion object {
        /** Clave de sustitución: máquinas de prensa de piernas. */
        private const val CLAVE_PRENSA: String = "PRENSA"

        /** Clave de sustitución: jalón al pecho (con tilde). */
        private const val CLAVE_JALON_ACENTUADA: String = "JALÓN"

        /** Clave de sustitución: jalón al pecho (sin tilde). */
        private const val CLAVE_JALON_SIN_ACENTO: String = "JALON"

        /** Clave de sustitución: extensión de cuádriceps (con tilde). */
        private const val CLAVE_EXTENSION_ACENTUADA: String = "EXTENSIÓN"

        /** Clave de sustitución: extensión de cuádriceps (sin tilde). */
        private const val CLAVE_EXTENSION_SIN_ACENTO: String = "EXTENSION"

        /** Palabra clave de los sustitutos de la prensa. */
        private const val PALABRA_SENTADILLA: String = "sentadilla"

        /** Palabra clave de los sustitutos del jalón (dominadas). */
        private const val PALABRA_DOMINADA: String = "dominada"

        /** Palabra clave de los sustitutos del jalón (remo). */
        private const val PALABRA_REMO: String = "remo"

        /** Palabra clave de los sustitutos de la extensión (zancadas). */
        private const val PALABRA_ZANCADA: String = "zancada"

        /** Palabra clave de los sustitutos de la extensión (sentadilla sissy). */
        private const val PALABRA_SENTADILLA_SISSY: String = "sentadilla sissy"
    }
}