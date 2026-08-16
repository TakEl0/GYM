/**
 * @file IngredienteToma.kt
 * @brief Modelo de dominio que representa un ingrediente individual dentro de una toma.
 *
 * Cada ingrediente pertenece a una toma del plan (desayuno, comida, merienda, etc.) y
 * aporta una cantidad concreta en gramos. El modelo expone propiedades calculadas que
 * materializan los macros del ingrediente a partir de su alimento de referencia.
 */
package com.gym.app.domain.model

/**
 * @class IngredienteToma
 * @brief Representa un alimento con su gramaje dentro de una toma concreta.
 *
 * El cálculo de kilocalorías y macronutrientes se realiza mediante regla de tres sobre
 * los valores por 100 g del alimento resuelto. Si el ingrediente no dispone de alimento
 * resuelto (bien porque `alimentoId` es nulo o porque la capa de datos no lo ha adjuntado),
 * todas las propiedades calculadas devuelven 0,0 para evitar cálculos inválidos.
 *
 * @property id Identificador único del ingrediente dentro del plan.
 * @property alimentoId Identificador del [Alimento] de referencia en el catálogo (opcional).
 * @property nombre Nombre descriptivo del ingrediente (p. ej. "Arroz blanco cocinado").
 * @property cantidadGramos Cantidad del ingrediente en gramos.
 * @property pesaje Tipo de pesaje del alimento (COCINADO o CRUDO) según las reglas Naturvitia.
 * @property origenPlan Indica si el ingrediente proviene del plan importado (true) o es una
 * adición manual del usuario (false).
 * @property alimentoResuelto Alimento del catálogo ya resuelto por la capa de datos, usado
 * para calcular los macros. Si es nulo, los macros se consideran 0,0.
 */
data class IngredienteToma(
    val id: String,
    val alimentoId: String? = null,
    val nombre: String,
    val cantidadGramos: Double,
    val pesaje: String,
    val origenPlan: Boolean = true,
    val alimentoResuelto: Alimento? = null
) {

    companion object {
        /** Valor del pesaje para alimentos pesados en cocinado. */
        const val PESAJE_COCINADO: String = "COCINADO"

        /** Valor del pesaje para alimentos pesados en crudo. */
        const val PESAJE_CRUDO: String = "CRUDO"

        /** Base de cálculo nutricional: 100 gramos de alimento. */
        const val BASE_GRAMOS: Double = 100.0
    }

    /**
     * @brief Kilocalorías aportadas por la cantidad gramos del ingrediente.
     * Se calcula como (kcalPor100g × cantidadGramos) / 100. Devuelve 0,0 si el
     * alimento no está resuelto.
     * @return Kilocalorías totales del ingrediente.
     */
    val kcal: Double
        get() {
            val alimento = alimentoResuelto ?: return 0.0
            return (alimento.kcalPor100g * cantidadGramos) / BASE_GRAMOS
        }

    /**
     * @brief Gramos de proteína aportados por el ingrediente.
     * Se calcula como (proteinasPor100g × cantidadGramos) / 100. Devuelve 0,0 si el
     * alimento no está resuelto.
     * @return Gramos de proteína del ingrediente.
     */
    val proteinasG: Double
        get() {
            val alimento = alimentoResuelto ?: return 0.0
            return (alimento.proteinasPor100g * cantidadGramos) / BASE_GRAMOS
        }

    /**
     * @brief Gramos de carbohidratos aportados por el ingrediente.
     * Se calcula como (carbohidratosPor100g × cantidadGramos) / 100. Devuelve 0,0 si
     * el alimento no está resuelto.
     * @return Gramos de carbohidratos del ingrediente.
     */
    val carbohidratosG: Double
        get() {
            val alimento = alimentoResuelto ?: return 0.0
            return (alimento.carbohidratosPor100g * cantidadGramos) / BASE_GRAMOS
        }

    /**
     * @brief Gramos de grasas aportados por el ingrediente.
     * Se calcula como (grasasPor100g × cantidadGramos) / 100. Devuelve 0,0 si el alimento
     * no está resuelto.
     * @return Gramos de grasas del ingrediente.
     */
    val grasasG: Double
        get() {
            val alimento = alimentoResuelto ?: return 0.0
            return (alimento.grasasPor100g * cantidadGramos) / BASE_GRAMOS
        }
}