/**
 * @file ResumenNutricional.kt
 * @brief Modelo de dominio que resume el estado nutricional diario del usuario.
 * Esta entidad pertenece a la capa de dominio y es totalmente independiente
 * de Android, de la base de datos y del backend remoto.
 */
package com.gym.app.domain.model

/**
 * @class ResumenNutricional
 * @brief Representa el resumen diario de kilocalorías y macronutrientes consumidos
 * frente a los objetivos marcados por el plan nutricional (método Naturvitia).
 *
 * El porcentaje de progreso es una propiedad calculada que permite a la capa de
 * presentación mostrar de forma inmediata cuánto se ha avanzado respecto al
 * objetivo calórico del día.
 *
 * @property kcalConsumidas Kilocalorías consumidas en el día.
 * @property kcalObjetivo Kilocalorías objetivo marcadas por el plan nutricional.
 * @property kcalRestantes Kilocalorías restantes hasta alcanzar el objetivo.
 * @property proteinasConsumidasG Gramos de proteína consumidos.
 * @property proteinasObjetivoG Gramos de proteína objetivo.
 * @property carbohidratosConsumidosG Gramos de carbohidratos consumidos.
 * @property carbohidratosObjetivoG Gramos de carbohidratos objetivo.
 * @property grasasConsumidasG Gramos de grasas consumidos.
 * @property grasasObjetivoG Gramos de grasas objetivo.
 */
data class ResumenNutricional(
    val kcalConsumidas: Double,
    val kcalObjetivo: Double,
    val kcalRestantes: Double,
    val proteinasConsumidasG: Double,
    val proteinasObjetivoG: Double,
    val carbohidratosConsumidosG: Double,
    val carbohidratosObjetivoG: Double,
    val grasasConsumidasG: Double,
    val grasasObjetivoG: Double
) {

    /**
     * @brief Porcentaje de progreso calórico del día (0..100).
     * Se calcula como (kcalConsumidas * 100) / kcalObjetivo, limitado a un máximo
     * de 100. Si el objetivo es 0 o negativo, el progreso devuelve 0 para evitar
     * divisiones inválidas.
     * @return Progreso expresado en porcentaje entero.
     */
    val progresoPorcentaje: Int
        get() = if (kcalObjetivo > 0) {
            ((kcalConsumidas * 100) / kcalObjetivo).toInt().coerceIn(0, 100)
        } else {
            0
        }
}