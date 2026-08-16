/**
 * @file RepositorioListaCompra.kt
 * @brief Puerto de repositorio de la lista de la compra en la capa de dominio.
 * Define el contrato para observar las listas semanales, guardarlas y marcar los
 * ítems como comprados durante la visita al supermercado.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.ListaCompra
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioListaCompra
 * @brief Contrato de acceso a las listas de la compra consolidadas por semana.
 */
interface RepositorioListaCompra {

    /**
     * @brief Observa de forma reactiva todas las listas de la compra del usuario.
     * @return Flujo reactivo con la lista de [ListaCompra] ordenadas por semana.
     */
    fun observarListas(): Flow<List<ListaCompra>>

    /**
     * @brief Guarda o actualiza una lista de la compra completa.
     * @param lista Lista de la compra a persistir.
     */
    suspend fun guardarLista(lista: ListaCompra)

    /**
     * @brief Marca o desmarca un ítem de la lista como comprado.
     * @param listaId Identificador de la lista que contiene el ítem.
     * @param itemId Identificador del ítem a actualizar.
     * @param comprado Nuevo estado de compra del ítem (`true` = comprado).
     */
    suspend fun marcarItemComprado(
        listaId: String,
        itemId: String,
        comprado: Boolean
    )
}