/**
 * @file ItemListaCompra.kt
 * @brief Modelo de dominio que representa un ingrediente consolidado en la lista de la compra.
 *
 * La lista de la compra se construye consolidando los gramos semanales de cada ingrediente
 * del plan y escalándolos a paquetes comerciales del supermercado elegido (p. ej.
 * "2 paquetes de panecillos integrales finos Mercadona").
 */
package com.gym.app.domain.model

/**
 * @class ItemListaCompra
 * @brief Representa un alimento concreto a comprar, con su cantidad consolidada.
 *
 * @property id Identificador único del ítem de compra.
 * @property nombreAlimento Nombre del alimento (p. ej. "Panecillos integrales finos").
 * @property cantidadGramos Gramos totales consolidados necesarios para la semana.
 * @property unidadComercial Unidad de venta en el supermercado (p. ej. "paquete", "lata").
 * @property cantidadPaquetes Número de paquetes/unidades comerciales necesarios para
 * cubrir [cantidadGramos].
 * @property supermercado Supermercado donde se adquiere el alimento (opcional).
 * @property tipoIngestaOrigen Tipos de ingesta de los que proviene el ingrediente
 * (p. ej. ["DESAYUNO", "MERIENDA"]), útil para auditorías del plan.
 * @property comprado Indica si el ítem ya ha sido comprado por el usuario.
 */
data class ItemListaCompra(
    val id: String,
    val nombreAlimento: String,
    val cantidadGramos: Double,
    val unidadComercial: String? = null,
    val cantidadPaquetes: Int = 0,
    val supermercado: String? = null,
    val tipoIngestaOrigen: List<String> = emptyList(),
    val comprado: Boolean = false
)