/**
 * @file Maquina.kt
 * @brief Modelo de dominio que representa una máquina o equipamiento del gimnasio.
 *
 * Las máquinas se clasifican por tipo de equipamiento y por los grupos musculares que
 * trabajan. La disponibilidad permite indicar cuándo una máquina está en reparación o
 * ocupada, y se utiliza para sugerir sustituciones en las rutinas.
 */
package com.gym.app.domain.model

/**
 * @class Maquina
 * @brief Representa una máquina o estación de entrenamiento del gimnasio.
 *
 * @property id Identificador único de la máquina.
 * @property nombre Nombre descriptivo (p. ej. "Prensa de piernas 45º").
 * @property grupoMuscular Grupos musculares que trabaja la máquina
 * (p. ej. ["CUADRICEPS", "GLUTEO"]).
 * @property tipoEquipamiento Tipo de equipamiento (MAQUINA_GUIADA, POLEA, BARRA,
 * MANCUERNAS).
 * @property disponible Indica si la máquina está operativa y libre para su uso (por
 * defecto `true`).
 * @property marca Marca del fabricante (p. ej. "Technogym", "Hammer Strength", "gym80", "Eleiko", "Nike Strength", "Rogue", "Watson").
 * @property modelo Modelo específico de la máquina (p. ej. "Artis Leg Press", "Iso-Lateral Wide Chest").
 * @property ejerciciosPosibles Lista de slugs de ejercicios que resuelve esta máquina.
 * @property sinonimos Lista de nombres alternativos (sinónimos) con los que se puede referir al ejercicio o máquina.
 */
data class Maquina(
    val id: String,
    val nombre: String,
    val grupoMuscular: List<String> = emptyList(),
    val tipoEquipamiento: String = TIPO_MAQUINA_GUIADA,
    val disponible: Boolean = true,
    val marca: String? = null,
    val modelo: String? = null,
    val ejerciciosPosibles: List<String> = emptyList(),
    val sinonimos: List<String> = emptyList()
) {

    companion object {
        /** Equipamiento: máquina guiada (selectorizado o con leva). */
        const val TIPO_MAQUINA_GUIADA: String = "MAQUINA_GUIADA"

        /** Equipamiento: estación de poleas. */
        const val TIPO_POLEA: String = "POLEA"

        /** Equipamiento: barra olímpica o barra recta. */
        const val TIPO_BARRA: String = "BARRA"

        /** Equipamiento: mancuernas o pesas rusas. */
        const val TIPO_MANCUERNAS: String = "MANCUERNAS"
    }
}
