/**
 * @file CalcularObjetivosNutricionalesCasoUso.kt
 * @brief Caso de uso de cálculo de los objetivos nutricionales del día a partir del perfil.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.model.ResumenNutricional

/**
 * @class CalcularObjetivosNutricionalesCasoUso
 * @brief Construye un [ResumenNutricional] con los objetivos calóricos y de
 * macronutrientes ya calculados por el [PerfilUsuario] (propiedades calculadas
 * `kcalObjetivo`, `proteinasObjetivoG`, `grasasObjetivoG` y
 * `carbohidratosObjetivoG`), dejando los valores consumidos a cero.
 *
 * Es un caso de uso de lógica pura: no depende de repositorios ni de frameworks,
 * por lo que resulta trivialmente testeable con datos en memoria. Devuelve un
 * [Result.failure] con [IllegalArgumentException] si el perfil no está completo
 * y, por tanto, sus propiedades calculadas no son fiables.
 */
class CalcularObjetivosNutricionalesCasoUso {

    /**
     * @brief Calcula los objetivos nutricionales del día a partir del perfil.
     * El resumen resultante parte de cero consumido y sus "restantes" coinciden
     * con el objetivo, de modo que la capa de presentación puede mostrar el
     * punto de partida del día.
     * @param perfil Perfil del usuario con los datos antropométricos completos.
     * @return [Result] con el [ResumenNutricional] de objetivos, o con el error
     * de validación si el perfil está incompleto.
     */
    suspend fun ejecutar(perfil: PerfilUsuario): Result<ResumenNutricional> {
        if (!perfil.esCompleto) {
            return Result.failure(
                IllegalArgumentException(
                    "El perfil no está completo para calcular los objetivos nutricionales."
                )
            )
        }

        val kcalObjetivo = perfil.kcalObjetivo
        return Result.success(
            ResumenNutricional(
                kcalConsumidas = 0.0,
                kcalObjetivo = kcalObjetivo,
                kcalRestantes = kcalObjetivo,
                proteinasConsumidasG = 0.0,
                proteinasObjetivoG = perfil.proteinasObjetivoG.toDouble(),
                carbohidratosConsumidosG = 0.0,
                carbohidratosObjetivoG = perfil.carbohidratosObjetivoG.toDouble(),
                grasasConsumidasG = 0.0,
                grasasObjetivoG = perfil.grasasObjetivoG.toDouble()
            )
        )
    }
}