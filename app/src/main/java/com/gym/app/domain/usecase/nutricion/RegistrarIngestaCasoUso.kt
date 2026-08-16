/**
 * @file RegistrarIngestaCasoUso.kt
 * @brief Caso de uso de registro de una ingesta consumida por el usuario.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.repository.RepositorioIngesta
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class RegistrarIngestaCasoUso
 * @brief Valida y persiste una nueva [IngestaRegistrada] a través del
 * [RepositorioIngesta], garantizando que el registro sea coherente antes de
 * formar parte de los cálculos de desvíos nutricionales del rebalanceo intra-día.
 *
 * Validaciones aplicadas:
 * - El nombre de la ingesta no puede estar vacío.
 * - Las kilocalorías no pueden ser negativas.
 * - Los macronutrientes (proteínas, carbohidratos y grasas) no pueden ser negativos.
 * - El tipo de ingesta y el momento del día no pueden estar vacíos.
 */
class RegistrarIngestaCasoUso(
    private val repositorioIngesta: RepositorioIngesta,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra una ingesta consumida en el sistema.
     * @param ingesta [IngestaRegistrada] con los datos de la ingesta a guardar.
     * @return [Result] con éxito (Unit) o con el error de validación o de persistencia.
     */
    suspend fun ejecutar(ingesta: IngestaRegistrada): Result<Unit> = withContext(dispatcher) {
        if (ingesta.nombre.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("El nombre de la ingesta no puede estar vacío.")
            )
        }
        if (ingesta.kcal < 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException("Las kilocalorías no pueden ser negativas.")
            )
        }
        if (ingesta.proteinasG < 0.0 || ingesta.carbohidratosG < 0.0 || ingesta.grasasG < 0.0) {
            return@withContext Result.failure(
                IllegalArgumentException("Los macronutrientes no pueden ser negativos.")
            )
        }
        if (ingesta.tipoIngesta.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("El tipo de ingesta no puede estar vacío.")
            )
        }
        if (ingesta.momentoDia.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("El momento del día no puede estar vacío.")
            )
        }
        try {
            repositorioIngesta.registrarIngesta(ingesta)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}