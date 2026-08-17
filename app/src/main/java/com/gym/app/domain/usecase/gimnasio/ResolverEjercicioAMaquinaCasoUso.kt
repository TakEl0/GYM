/**
 * @file ResolverEjercicioAMaquinaCasoUso.kt
 * @brief Caso de uso que resuelve un ejercicio del plan PDF contra una máquina del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.MapeoAprendido
import com.gym.app.domain.repository.RepositorioMapeoAprendido

/**
 * @class ResolverEjercicioAMaquinaCasoUso
 * @brief Orquesta la resolución de un nombre de ejercicio del plan del nutricionista a
 * una máquina real del gimnasio siguiendo la estrategia en cascada del ADR 0004:
 *
 * 1. Si el [RepositorioMapeoAprendido] está disponible, se consultan primero los mapeos
 *    aprendidos por el usuario (correcciones manuales persistidas). Si existe un
 *    [MapeoAprendido] para el nombre normalizado, se usa con confianza máxima y origen
 *    [OrigenMapeo.MANUAL], independientemente de lo que diga el catálogo.
 * 2. Si no hay aprendizaje, se delega en el [MotorMapeoEjercicioAMaquina] local
 *    (EXACTO → SINONIMO → FAMILIA), que devuelve `null` cuando debe intervenir la IA.
 *
 * El caso de uso es una función pura sobre el motor y el repositorio: no depende de
 * frameworks ni de Android, por lo que es directamente testeable.
 *
 * @property motor Motor local de mapeo ejercicio → máquina.
 * @property repositorioAprendizaje Repositorio de mapeos aprendidos (opcional; si es
 * `null` se omite la consulta de aprendizaje y solo se usa el motor local).
 */
class ResolverEjercicioAMaquinaCasoUso(
    private val motor: MotorMapeoEjercicioAMaquina,
    private val repositorioAprendizaje: RepositorioMapeoAprendido? = null
) {

    /**
     * @brief Resuelve un ejercicio contra el parque de máquinas indicado.
     * @param nombreEjercicio Nombre del ejercicio tal y como aparece en el plan PDF.
     * @param maquinas Máquinas candidatas (catálogo real o gimnasio del usuario).
     * @return [ResolucionMapeo] con la máquina resuelta, o `null` si no hay resolución
     * (ni aprendizaje ni reglas locales); en ese caso intervendrá la IA.
     */
    suspend fun ejecutar(nombreEjercicio: String, maquinas: List<Maquina>): ResolucionMapeo? {
        val nombreNormalizado = MotorMapeoEjercicioAMaquina.normalizar(nombreEjercicio)
        if (nombreNormalizado.isBlank()) return null

        // 1) Prioridad máxima: aprendizaje del usuario (correcciones manuales).
        val aprendido: MapeoAprendido? = repositorioAprendizaje?.buscar(nombreNormalizado)
        if (aprendido != null) {
            return ResolucionMapeo(
                nombrePdf = nombreEjercicio,
                maquinaId = aprendido.maquinaId,
                confianza = CONFIANZA_APRENDIDO,
                origen = OrigenMapeo.MANUAL
            )
        }

        // 2) Reglas locales (EXACTO → SINONIMO → FAMILIA).
        return motor.resolver(nombreEjercicio, maquinas)
    }

    companion object {
        /** Confianza máxima para un mapeo aprendido confirmado por el usuario. */
        const val CONFIANZA_APRENDIDO: Float = 1.0f
    }
}