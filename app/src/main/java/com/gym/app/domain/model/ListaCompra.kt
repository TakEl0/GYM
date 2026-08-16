/**
 * @file ListaCompra.kt
 * @brief Modelo de dominio que representa la lista de la compra consolidada de una semana.
 *
 * La lista se genera a partir del plan nutricional semanal, consolidando los gramos de
 * cada ingrediente, escalándolos a paquetes comerciales y agrupándolos por supermercado.
 * Nunca inventa ingredientes: todos los ítems provienen del plan de comidas.
 */
package com.gym.app.domain.model

import java.time.LocalDate

/**
 * @class ListaCompra
 * @brief Representa la lista de la compra de una semana, con sus ítems y supermercados.
 *
 * @property id Identificador único de la lista.
 * @property semanaInicio Fecha de inicio de la semana a la que pertenece la lista (lunes).
 * @property items Ítems consolidados de la compra.
 * @property supermercados Supermercados implicados en la lista (resultado de la agrupación
 * de los ítems por supermercado preferido).
 */
data class ListaCompra(
    val id: String,
    val semanaInicio: LocalDate,
    val items: List<ItemListaCompra> = emptyList(),
    val supermercados: List<String> = emptyList()
) {

    /**
     * @brief Filtra los ítems de la lista pertenecientes a un supermercado concreto.
     * @param supermercado Nombre del supermercado a filtrar.
     * @return Lista de [ItemListaCompra] cuyo supermercado coincide con el solicitado.
     */
    fun itemsPorSupermercado(supermercado: String): List<ItemListaCompra> =
        items.filter { it.supermercado == supermercado }

    /**
     * @brief Número de ítems de la lista que aún no han sido comprados.
     * @return Cantidad de ítems con `comprado == false`.
     */
    val totalItemsPendientes: Int
        get() = items.count { !it.comprado }
}