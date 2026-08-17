/**
 * @file MapeoAprendido.kt
 * @brief Modelo de dominio que representa un mapeo ejercicio → máquina aprendido.
 */
package com.gym.app.domain.model

/**
 * @data class MapeoAprendido
 * @brief Persiste una corrección manual del usuario en la resolución de un ejercicio
 * del plan PDF contra una máquina del gimnasio.
 *
 * Cuando el usuario corrige manualmente a qué máquina corresponde un ejercicio, ese
 * mapeo se guarda (aprendizaje) y la próxima vez que aparezca el mismo ejercicio
 * (con el mismo nombre normalizado) se resuelve offline con prioridad máxima y origen
 * [com.gym.app.domain.usecase.gimnasio.OrigenMapeo.MANUAL], sin necesidad de IA ni de
 * reglas locales.
 *
 * @property nombreNormalizado Nombre del ejercicio ya normalizado por el motor de mapeo
 * (clave de búsqueda; p. ej. "femoral tumbado").
 * @property maquinaId Identificador de la máquina elegida por el usuario.
 * @property fecha Fecha de la corrección en formato epoch millis.
 */
data class MapeoAprendido(
    val nombreNormalizado: String,
    val maquinaId: String,
    val fecha: Long
)