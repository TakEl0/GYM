/**
 * @file GuardarPerfilCasoUso.kt
 * @brief Caso de uso de guardado del perfil completo del usuario.
 */
package com.gym.app.domain.usecase.perfil

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class GuardarPerfilCasoUso
 * @brief Valida y persiste el perfil completo del usuario (nombre, datos
 * antropométricos y objetivos nutricionales) delegando en
 * [RepositorioPerfil.guardarPerfil].
 *
 * Es la operación de escritura global del perfil: a diferencia de
 * [ActualizarObjetivosPerfilCasoUso] (que solo toca los objetivos), este caso
 * de uso permite actualizar también el nombre público del usuario, garantizando
 * que las pantallas de Dashboard y Perfil muestren siempre el nombre real.
 *
 * Validaciones aplicadas (método Naturvitia):
 * - Nombre obligatorio y con al menos 2 caracteres.
 * - Alias opcional; si se informa, debe tener entre 2 y 24 caracteres y solo
 *   letras, números, guiones y guiones bajos (sin espacios).
 * - Peso objetivo mayor que 0 kg.
 * - Altura en el rango (0, 250] cm.
 * - Edad en el rango [10, 100] años.
 * - Sexo restringido a HOMBRE o MUJER.
 * - Factor de actividad restringido a SEDENTARIO, LIGERO, MODERADO o FUERTE.
 * - Objetivo restringido a VOLUMEN, DEFINICION o MANTENIMIENTO.
 */
class GuardarPerfilCasoUso(
    private val repositorioPerfil: RepositorioPerfil,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Guarda el perfil completo validando sus campos.
     * @param perfil Perfil a persistir.
     * @return [Result] con éxito (Unit) o con el error de validación o persistencia.
     */
    suspend fun ejecutar(perfil: PerfilUsuario): Result<Unit> = withContext(dispatcher) {
        val nombre = perfil.nombre.trim()
        if (nombre.length < 2) {
            return@withContext Result.failure(
                IllegalArgumentException("El nombre debe tener al menos 2 caracteres.")
            )
        }

        // El alias es opcional: si se informa, se valida el formato permitido.
        val alias = perfil.alias?.trim()?.takeIf { it.isNotBlank() }
        if (alias != null && (alias.length < 2 || alias.length > 24 || !alias.matches(Regex("^[a-zA-Z0-9_-]+$")))) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "El alias debe tener entre 2 y 24 caracteres y solo letras, números, guiones o guiones bajos."
                )
            )
        }

        val peso = perfil.pesoObjetivoKg
            ?: return@withContext Result.failure(
                IllegalArgumentException("El peso objetivo no puede estar vacío.")
            )
        if (peso <= 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException("El peso objetivo debe ser mayor que 0 kg.")
            )
        }

        val altura = perfil.alturaCm
            ?: return@withContext Result.failure(
                IllegalArgumentException("La altura no puede estar vacía.")
            )
        if (altura <= 0.0 || altura > 250.0) {
            return@withContext Result.failure(
                IllegalArgumentException("La altura debe estar entre 0 y 250 cm.")
            )
        }

        val edad = perfil.edad
            ?: return@withContext Result.failure(
                IllegalArgumentException("La edad no puede estar vacía.")
            )
        if (edad < 10 || edad > 100) {
            return@withContext Result.failure(
                IllegalArgumentException("La edad debe estar entre 10 y 100 años.")
            )
        }

        val sexo = perfil.sexo
            ?: return@withContext Result.failure(
                IllegalArgumentException("El sexo no puede estar vacío.")
            )
        if (sexo != PerfilUsuario.SEXO_HOMBRE && sexo != PerfilUsuario.SEXO_MUJER) {
            return@withContext Result.failure(
                IllegalArgumentException("El sexo debe ser HOMBRE o MUJER.")
            )
        }

        val factor = perfil.factorActividad
            ?: return@withContext Result.failure(
                IllegalArgumentException("El factor de actividad no puede estar vacío.")
            )
        val factoresPermitidos = listOf("SEDENTARIO", "LIGERO", "MODERADO", "FUERTE")
        if (factor !in factoresPermitidos) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "El factor de actividad debe ser SEDENTARIO, LIGERO, MODERADO o FUERTE."
                )
            )
        }

        val objetivo = perfil.objetivo
            ?: return@withContext Result.failure(
                IllegalArgumentException("El objetivo no puede estar vacío.")
            )
        val objetivosPermitidos = listOf(
            PerfilUsuario.OBJETIVO_VOLUMEN,
            PerfilUsuario.OBJETIVO_DEFINICION,
            PerfilUsuario.OBJETIVO_MANTENIMIENTO
        )
        if (objetivo !in objetivosPermitidos) {
            return@withContext Result.failure(
                IllegalArgumentException(
                    "El objetivo debe ser VOLUMEN, DEFINICION o MANTENIMIENTO."
                )
            )
        }

        try {
            repositorioPerfil.guardarPerfil(
                perfil.copy(
                    nombre = nombre,
                    alias = alias,
                    pesoObjetivoKg = peso,
                    alturaCm = altura,
                    edad = edad,
                    sexo = sexo,
                    factorActividad = factor,
                    objetivo = objetivo
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}