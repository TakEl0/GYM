/**
 * @file RegistrarComidaCasoUso.kt
 * @brief Caso de uso de registro de una nueva comida o ingesta nutricional.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Comida
import com.gym.app.domain.repository.RepositorioComida
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class RegistrarComidaCasoUso
 * @brief Valida y persiste una nueva [Comida] a través del [RepositorioComida],
 * garantizando que los valores calóricos y de macronutrientes sean coherentes.
 */
class RegistrarComidaCasoUso(
    private val repositorioComida: RepositorioComida,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra una comida en el sistema.
     * Valida que las kilocalorías y los macronutrientes (proteínas, carbohidratos
     * y grasas) no sean negativos. Ante datos inválidos devuelve un
     * [Result.failure] con [IllegalArgumentException].
     * @param comida [Comida] con los datos de la ingesta a guardar.
     * @return [Result] con éxito (Unit) o con el error producido.
     */
    suspend fun ejecutar(comida: Comida): Result<Unit> = withContext(dispatcher) {
        if (comida.kcal < 0) {
            return@withContext Result.failure(
                IllegalArgumentException("Las kilocalorías no pueden ser negativas.")
            )
        }
        if (comida.proteinasG < 0.0 || comida.carbohidratosG < 0.0 || comida.grasasG < 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException("Los macronutrientes no pueden ser negativos.")
            )
        }
        try {
            repositorioComida.guardarComida(comida)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}