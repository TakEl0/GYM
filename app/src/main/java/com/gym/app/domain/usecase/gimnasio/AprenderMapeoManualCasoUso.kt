/**
 * @file AprenderMapeoManualCasoUso.kt
 * @brief Caso de uso que persiste una corrección manual del usuario en el mapeo de
 * ejercicios del plan PDF a máquinas del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.MapeoAprendido
import com.gym.app.domain.repository.RepositorioMapeoAprendido

/**
 * @class AprenderMapeoManualCasoUso
 * @brief Guarda una corrección del usuario: cuando la resolución automática (motor local
 * o IA) no es correcta, el usuario indica la máquina real y este caso de uso persiste el
 * mapeo para que las siguientes veces se resuelva offline con confianza máxima.
 *
 * El nombre del ejercicio se **normaliza antes de guardar** mediante
 * [MotorMapeoEjercicioAMaquina.normalizar], de modo que la clave coincide siempre con la
 * que usará [ResolverEjercicioAMaquinaCasoUso] al consultar el aprendizaje.
 *
 * @property repositorioAprendizaje Repositorio donde se persiste el mapeo aprendido.
 */
class AprenderMapeoManualCasoUso(
    private val repositorioAprendizaje: RepositorioMapeoAprendido
) {

    /**
     * @brief Normaliza y guarda un mapeo manual del usuario.
     * @param nombreEjercicio Nombre del ejercicio tal y como aparece en el plan PDF.
     * @param maquinaId Identificador de la máquina elegida manualmente por el usuario.
     * @return [Result] con el [MapeoAprendido] persistido, o con el error de validación
     * o de persistencia.
     */
    suspend fun ejecutar(nombreEjercicio: String, maquinaId: String): Result<MapeoAprendido> {
        val nombreNormalizado = MotorMapeoEjercicioAMaquina.normalizar(nombreEjercicio)
        if (nombreNormalizado.isBlank()) {
            return Result.failure(
                IllegalArgumentException("El nombre del ejercicio no puede estar vacío.")
            )
        }
        if (maquinaId.isBlank()) {
            return Result.failure(
                IllegalArgumentException("El identificador de la máquina no puede estar vacío.")
            )
        }

        val mapeo = MapeoAprendido(
            nombreNormalizado = nombreNormalizado,
            maquinaId = maquinaId,
            fecha = System.currentTimeMillis()
        )

        return try {
            repositorioAprendizaje.guardar(mapeo)
            Result.success(mapeo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}