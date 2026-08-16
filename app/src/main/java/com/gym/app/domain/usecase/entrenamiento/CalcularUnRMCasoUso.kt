/**
 * @file CalcularUnRMCasoUso.kt
 * @brief Caso de uso de cálculo del máximo de una repetición (1RM).
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.CalculoUnRM

/**
 * @class CalcularUnRMCasoUso
 * @brief Valida los datos de entrada de una estimación de 1RM (peso levantado y
 * repeticiones ejecutadas) y devuelve una instancia de [CalculoUnRM] preparada
 * para su cálculo.
 *
 * La estimación numérica del 1RM se obtiene mediante [CalculoUnRM.calcular],
 * que promedia las fórmulas de **Epley** (W × (1 + r/30)) y **Brzycki**
 * (W × 36/(37 - r)). Este caso de uso garantiza que los datos de entrada son
 * válidos antes de invocar el modelo: peso estrictamente positivo y repeticiones
 * en el rango [1, 35].
 *
 * Es un caso de uso de lógica pura (sin repositorios), trivialmente testeable.
 */
class CalcularUnRMCasoUso {

    /**
     * @brief Valida y encapsula los datos de entrada para el cálculo de 1RM.
     * @param pesoKg Carga levantada en kilogramos (debe ser mayor que 0).
     * @param repeticiones Número de repeticiones ejecutadas (debe estar en 1..35).
     * @return [Result] con la [CalculoUnRM] validada (cuyo valor estimado se
     * obtiene mediante `CalculoUnRM.calcular(pesoKg, repeticiones)`), o con el
     * error de validación.
     */
    suspend fun ejecutar(pesoKg: Double, repeticiones: Int): Result<CalculoUnRM> {
        if (pesoKg <= 0.0) {
            return Result.failure(
                IllegalArgumentException("El peso debe ser mayor que 0 kg.")
            )
        }
        if (repeticiones < 1 || repeticiones > 35) {
            return Result.failure(
                IllegalArgumentException("Las repeticiones deben estar entre 1 y 35.")
            )
        }
        return Result.success(
            CalculoUnRM(pesoKg = pesoKg, repeticiones = repeticiones)
        )
    }
}