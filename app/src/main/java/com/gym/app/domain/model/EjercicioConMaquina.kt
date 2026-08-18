/**
 * @file EjercicioConMaquina.kt
 * @brief Modelo de dominio auxiliar de la sesión de entrenamiento en vivo.
 *
 * Combina el bloque prescrito de una rutina con su ejercicio resuelto y la
 * máquina real (marca · modelo) del gimnasio sobre la que se ejecuta. Es el
 * elemento que consume la interfaz de usuario durante el entrenamiento en vivo
 * para mostrar el nombre del ejercicio, la máquina concreta y las series
 * prescritas del plan.
 */
package com.gym.app.domain.model

/**
 * @class EjercicioConMaquina
 * @brief Pareja bloque + ejercicio + máquina real resuelta para la sesión en vivo.
 *
 * @property bloque Prescripción de series del ejercicio dentro de la rutina
 * ([BloqueRutina]: series, repeticiones, descanso y carga orientativa).
 * @property ejercicio Ejercicio resuelto del catálogo (puede ser provisional si
 * el bloque referencia un ejercicio aún no catalogado).
 * @property maquina Máquina real del gimnasio sobre la que se ejecuta el
 * ejercicio (marca · modelo), o `null` cuando se trata de peso libre o el
 * ejercicio no se pudo resolver contra el parque de maquinaria.
 */
data class EjercicioConMaquina(
    val bloque: BloqueRutina,
    val ejercicio: Ejercicio,
    val maquina: Maquina?
)