/**
 * @file ActualizarObjetivosPerfilCasoUso.kt
 * @brief Caso de uso de actualización de los objetivos y datos antropométricos del perfil.
 */
package com.gym.app.domain.usecase.perfil

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class ActualizarObjetivosPerfilCasoUso
 * @brief Valida y persiste los cambios de los datos antropométricos y de objetivos
 * del usuario (peso objetivo, altura, edad, sexo, factor de actividad y objetivo
 * nutricional) delegando en [RepositorioPerfil.actualizarObjetivos].
 *
 * La validación aplica las reglas del método Naturvitia:
 * - Altura en el rango (0, 250] centímetros.
 * - Edad en el rango [10, 100] años.
 * - Factor de actividad restringido a SEDENTARIO, LIGERO, MODERADO y FUERTE.
 * - Objetivo restringido a VOLUMEN, DEFINICION y MANTENIMIENTO.
 * - Sexo restringido a HOMBRE y MUJER (fórmula de Mifflin-St Jeor).
 *
 * Todos los parámetros son opcionales en la firma, pero la operación exige que
 * estén informados: si alguno es `null` o queda fuera de rango, se devuelve un
 * [Result.failure] con [IllegalArgumentException] y no se toca el repositorio.
 */
class ActualizarObjetivosPerfilCasoUso(
    private val repositorioPerfil: RepositorioPerfil,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Actualiza los objetivos y datos antropométricos del perfil.
     * @param id Identificador único del perfil.
     * @param pesoObjetivoKg Nuevo peso de referencia en kilogramos.
     * @param alturaCm Nueva estatura en centímetros (0 < altura <= 250).
     * @param edad Nueva edad en años (10 <= edad <= 100).
     * @param sexo Sexo biológico (HOMBRE o MUJER).
     * @param factorActividad Nivel de actividad (SEDENTARIO, LIGERO, MODERADO, FUERTE).
     * @param objetivo Objetivo nutricional (VOLUMEN, DEFINICION, MANTENIMIENTO).
     * @return [Result] con éxito (Unit) o con el error de validación o de persistencia.
     */
    suspend fun ejecutar(
        id: String,
        pesoObjetivoKg: Double?,
        alturaCm: Double?,
        edad: Int?,
        sexo: String?,
        factorActividad: String?,
        objetivo: String?
    ): Result<Unit> = withContext(dispatcher) {
        val peso = pesoObjetivoKg
            ?: return@withContext Result.failure(
                IllegalArgumentException("El peso objetivo no puede estar vacío.")
            )
        if (peso <= 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException("El peso objetivo debe ser mayor que 0 kg.")
            )
        }

        val altura = alturaCm
            ?: return@withContext Result.failure(
                IllegalArgumentException("La altura no puede estar vacía.")
            )
        if (altura <= 0.0 || altura > 250.0) {
            return@withContext Result.failure(
                IllegalArgumentException("La altura debe estar entre 0 y 250 cm.")
            )
        }

        val edadValida = edad
            ?: return@withContext Result.failure(
                IllegalArgumentException("La edad no puede estar vacía.")
            )
        if (edadValida < 10 || edadValida > 100) {
            return@withContext Result.failure(
                IllegalArgumentException("La edad debe estar entre 10 y 100 años.")
            )
        }

        val sexoValido = sexo
            ?: return@withContext Result.failure(
                IllegalArgumentException("El sexo no puede estar vacío.")
            )
        if (sexoValido != PerfilUsuario.SEXO_HOMBRE && sexoValido != PerfilUsuario.SEXO_MUJER) {
            return@withContext Result.failure(
                IllegalArgumentException("El sexo debe ser HOMBRE o MUJER.")
            )
        }

        val factorValido = factorActividad
            ?: return@withContext Result.failure(
                IllegalArgumentException("El factor de actividad no puede estar vacío.")
            )
        val factoresPermitidos = listOf(
            "SEDENTARIO", "LIGERO", "MODERADO", "FUERTE"
        )
        if (factorValido !in factoresPermitidos) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "El factor de actividad debe ser SEDENTARIO, LIGERO, MODERADO o FUERTE."
                )
            )
        }

        val objetivoValido = objetivo
            ?: return@withContext Result.failure(
                IllegalArgumentException("El objetivo no puede estar vacío.")
            )
        val objetivosPermitidos = listOf(
            PerfilUsuario.OBJETIVO_VOLUMEN,
            PerfilUsuario.OBJETIVO_DEFINICION,
            PerfilUsuario.OBJETIVO_MANTENIMIENTO
        )
        if (objetivoValido !in objetivosPermitidos) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "El objetivo debe ser VOLUMEN, DEFINICION o MANTENIMIENTO."
                )
            )
        }

        try {
            repositorioPerfil.actualizarObjetivos(
                id = id,
                pesoObjetivoKg = peso,
                alturaCm = altura,
                edad = edadValida,
                sexo = sexoValido,
                factorActividad = factorValido,
                objetivo = objetivoValido
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}