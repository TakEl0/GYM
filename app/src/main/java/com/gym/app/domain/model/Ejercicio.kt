/**
 * @file Ejercicio.kt
 * @brief Modelo de dominio que representa un ejercicio del catálogo de entrenamiento.
 *
 * El ejercicio se describe por su grupo muscular principal y secundario, el equipamiento
 * necesario y, opcionalmente, la máquina concreta del gimnasio a la que está asociado.
 * Este modelo es la base del catálogo usado por el motor de sustituciones de maquinaria.
 */
package com.gym.app.domain.model

/**
 * @class Ejercicio
 * @brief Representa un ejercicio con su clasificación muscular y de equipamiento.
 *
 * @property id Identificador único del ejercicio.
 * @property nombre Nombre descriptivo del ejercicio (p. ej. "Prensa de piernas").
 * @property grupoMuscularPrincipal Grupo muscular principal trabajado
 * (p. ej. "CUADRICEPS").
 * @property grupoMuscularSecundario Grupo muscular secundario implicado (opcional).
 * @property maquinaId Identificador de la [Maquina] concreta del gimnasio necesaria
 * para ejecutar el ejercicio (opcional; nulo si se usa equipamiento libre).
 * @property equipamiento Tipo de equipamiento requerido (MAQUINA_GUIADA, POLEA,
 * BARRA, MANCUERNAS o CALISTENIA).
 * @property instrucciones Instrucciones de ejecución en texto (opcional).
 */
data class Ejercicio(
    val id: String,
    val nombre: String,
    val grupoMuscularPrincipal: String,
    val grupoMuscularSecundario: String? = null,
    val maquinaId: String? = null,
    val equipamiento: String = EQUIPAMIENTO_MAQUINA_GUIADA,
    val instrucciones: String? = null
) {

    companion object {
        /** Equipamiento: máquina guiada. */
        const val EQUIPAMIENTO_MAQUINA_GUIADA: String = "MAQUINA_GUIADA"

        /** Equipamiento: poleas. */
        const val EQUIPAMIENTO_POLEA: String = "POLEA"

        /** Equipamiento: barra. */
        const val EQUIPAMIENTO_BARRA: String = "BARRA"

        /** Equipamiento: mancuernas. */
        const val EQUIPAMIENTO_MANCUERNAS: String = "MANCUERNAS"

        /** Equipamiento: ejercicio de calistenia (peso corporal). */
        const val EQUIPAMIENTO_CALISTENIA: String = "CALISTENIA"
    }
}