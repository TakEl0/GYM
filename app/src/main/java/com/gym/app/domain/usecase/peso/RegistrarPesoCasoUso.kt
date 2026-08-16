/**
 * @file RegistrarPesoCasoUso.kt
 * @brief Caso de uso de registro de una nueva medición de peso corporal.
 */
package com.gym.app.domain.usecase.peso

import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * @class RegistrarPesoCasoUso
 * @brief Valida y persiste una nueva medición de peso corporal a través del
 * [RepositorioPeso].
 *
 * El parámetro [userId] se conserva en la firma pública por coherencia con el
 * contrato de dominio; la implementación concreta del repositorio resuelve el
 * usuario activo internamente al persistir el registro.
 */
class RegistrarPesoCasoUso(
    private val repositorioPeso: RepositorioPeso,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra una nueva medición de peso para el día actual.
     * Valida que el peso esté en el rango (0, 500] kg y que el porcentaje de grasa
     * corporal, si se indica, esté entre 0 y 100. Ante datos inválidos devuelve un
     * [Result.failure] con [IllegalArgumentException].
     * @param pesoKg Peso corporal en kilogramos.
     * @param grasaCorporal Porcentaje de grasa corporal (opcional).
     * @param userId Identificador del usuario propietario del registro.
     * @return [Result] con éxito (Unit) o con el error producido.
     */
    suspend fun ejecutar(pesoKg: Double, grasaCorporal: Double?, userId: String): Result<Unit> =
        withContext(dispatcher) {
            if (pesoKg <= 0.0 || pesoKg > 500.0) {
                return@withContext Result.failure(
                    IllegalArgumentException("El peso debe ser mayor que 0 y menor o igual que 500 kg.")
                )
            }
            if (grasaCorporal != null && (grasaCorporal < 0.0 || grasaCorporal > 100.0)) {
                return@withContext Result.failure(
                    IllegalArgumentException("El porcentaje de grasa corporal debe estar entre 0 y 100.")
                )
            }
            try {
                val registro = RegistroPeso(
                    fecha = LocalDate.now(),
                    pesoKg = pesoKg,
                    grasaCorporalPorcentaje = grasaCorporal
                )
                repositorioPeso.guardarRegistro(registro)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}