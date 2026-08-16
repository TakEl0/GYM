/**
 * @file GenerarPlanComidasCasoUso.kt
 * @brief Caso de uso de generación de un plan de comidas clonado para una fecha concreta.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.PlanComida
import java.time.LocalDate
import java.util.UUID

/**
 * @class GenerarPlanComidasCasoUso
 * @brief Clona un plan de comidas base (plantilla de un día del nutricionista)
 * para una fecha determinada, generando identificadores nuevos para el plan,
 * sus tomas y sus ingredientes.
 *
 * La clonación permite reutilizar la misma estructura de dieta en distintos días
 * de la semana sin compartir identidades: cada copia es un plan independiente
 * que puede ser rebalanceado, registrado o eliminado por separado. Se conservan
 * el nombre, los gramajes, los ingredientes y el origen de importación.
 *
 * Es un caso de uso de lógica pura (sin repositorios), trivialmente testeable.
 */
class GenerarPlanComidasCasoUso {

    /**
     * @brief Genera una copia del plan base asignada a la fecha indicada.
     * Se generan nuevos UUID para el [PlanComida], cada [com.gym.app.domain.model.Toma]
     * y cada [com.gym.app.domain.model.IngredienteToma]. El resto de campos se
     * conservan intactos.
     * @param planBase Plan de comidas que actúa como plantilla del día.
     * @param fecha Fecha a la que se asignará el plan clonado.
     * @return [Result] con el [PlanComida] clonado para la fecha solicitada.
     */
    suspend fun ejecutar(planBase: PlanComida, fecha: LocalDate): Result<PlanComida> {
        val tomasClonadas = planBase.tomas.map { toma ->
            toma.copy(
                id = UUID.randomUUID().toString(),
                ingredientes = toma.ingredientes.map { ingrediente ->
                    ingrediente.copy(id = UUID.randomUUID().toString())
                }
            )
        }

        val planClonado = planBase.copy(
            id = UUID.randomUUID().toString(),
            fecha = fecha,
            tomas = tomasClonadas
        )
        return Result.success(planClonado)
    }
}