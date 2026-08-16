/**
 * @file CalculoUnRM.kt
 * @brief Modelo de dominio para el cálculo del máximo de una repetición (1RM).
 *
 * Agrupa las dos fórmulas de estimación del 1RM más utilizadas en ciencias del deporte:
 * la de **Epley** y la de **Brzycki**. El resultado consolidado se obtiene como el
 * promedio de ambas estimaciones, proporcionando una referencia más estable y robusta.
 */
package com.gym.app.domain.model

/**
 * @class CalculoUnRM
 * @brief Encapsula los datos de entrada (peso y repeticiones) de una estimación de 1RM.
 *
 * @property pesoKg Carga levantada en kilogramos.
 * @property repeticiones Número de repeticiones ejecutadas con esa carga.
 */
data class CalculoUnRM(
    val pesoKg: Double,
    val repeticiones: Int
) {

    companion object {
        /** Denominador de Epley: número de repeticiones de referencia (30). */
        private const val EPLEY_DENOMINADOR: Double = 30.0

        /** Numerador de Brzycki (36) y denominador base (37). */
        private const val BRZYCKI_NUMERADOR: Double = 36.0
        private const val BRZYCKI_DENOMINADOR_BASE: Int = 37

        /** Número máximo de repeticiones para el que Brzycki es fiable. */
        private const val REPETICIONES_MAX_BRZYCKI: Int = 36

        /**
         * @brief Estima el 1RM mediante la fórmula de Epley.
         *
         * Fórmula: 1RM = peso × (1 + repeticiones / 30).
         * Es válida para rangos habituales de repeticiones (1-30) y se comporta de forma
         * estable incluso con repeticiones elevadas.
         *
         * @param pesoKg Carga levantada en kilogramos.
         * @param repeticiones Número de repeticiones ejecutadas.
         * @return Estimación del 1RM en kilogramos. Devuelve 0,0 si el peso o las
         * repeticiones no son válidos (peso negativo o menos de 1 repetición).
         */
        fun epley(pesoKg: Double, repeticiones: Int): Double {
            if (pesoKg < 0.0 || repeticiones < 1) return 0.0
            return pesoKg * (1.0 + repeticiones / EPLEY_DENOMINADOR)
        }

        /**
         * @brief Estima el 1RM mediante la fórmula de Brzycki.
         *
         * Fórmula: 1RM = peso × 36 / (37 - repeticiones).
         * Solo es fiable hasta 36 repeticiones (a partir de ahí el denominador se anula o
         * se vuelve negativo). Por seguridad, cuando el número de repeticiones supera ese
         * límite se delega en la fórmula de Epley, que sí permanece válida.
         *
         * @param pesoKg Carga levantada en kilogramos.
         * @param repeticiones Número de repeticiones ejecutadas.
         * @return Estimación del 1RM en kilogramos. Devuelve 0,0 si el peso o las
         * repeticiones no son válidos.
         */
        fun brzycki(pesoKg: Double, repeticiones: Int): Double {
            if (pesoKg < 0.0 || repeticiones < 1) return 0.0
            if (repeticiones >= REPETICIONES_MAX_BRZYCKI) {
                return epley(pesoKg, repeticiones)
            }
            return pesoKg * BRZYCKI_NUMERADOR /
                (BRZYCKI_DENOMINADOR_BASE - repeticiones)
        }

        /**
         * @brief Estima el 1RM como promedio de las fórmulas de Epley y Brzycki.
         *
         * Combinar ambas estimaciones reduce el sesgo individual de cada fórmula y
         * proporciona una referencia más conservadora y estable para planificar cargas.
         *
         * @param pesoKg Carga levantada en kilogramos.
         * @param repeticiones Número de repeticiones ejecutadas.
         * @return Promedio de las estimaciones de Epley y Brzycki en kilogramos.
         */
        fun calcular(pesoKg: Double, repeticiones: Int): Double {
            val estimacionEpley = epley(pesoKg, repeticiones)
            val estimacionBrzycki = brzycki(pesoKg, repeticiones)
            return (estimacionEpley + estimacionBrzycki) / 2.0
        }
    }
}