/**
 * @file AjusteToma.kt
 * @brief Modelo de dominio que describe el resultado de un ajuste de rebalanceo intra-día.
 *
 * Cuando el desvío nutricional supera la tolerancia, el módulo de rebalanceo propone
 * un ajuste sobre las tomas pendientes del día. Este modelo encapsula qué cambios se
 * sugieren y cómo quedan las tomas revisadas tras aplicarlos.
 */
package com.gym.app.domain.model

/**
 * @class AjusteToma
 * @brief Representa un ajuste propuesto sobre una o varias tomas del plan diario.
 *
 * Los cambios se describen como instrucciones legibles en texto plano (p. ej.
 * "Retirar 100 g de boniato de la cena" o "Añadir 1 lata de atún al natural a la
 * merienda") y las tomas revisadas contienen el resultado final tras aplicar la
 * corrección.
 *
 * @property tipoIngesta Tipo de ingesta sobre la que se aplica el ajuste principal
 * (p. ej. MERIENDA o CENA).
 * @property cambios Lista de instrucciones de cambio legibles para el usuario.
 * @property tomasRevisadas Tomas del día ya modificadas con el ajuste aplicado.
 */
data class AjusteToma(
    val tipoIngesta: String,
    val cambios: List<String> = emptyList(),
    val tomasRevisadas: List<Toma> = emptyList()
)