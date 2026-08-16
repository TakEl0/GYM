/**
 * @file MarcarItemCompradoCasoUso.kt
 * @brief Caso de uso de marcado de un ítem de la lista de la compra como comprado.
 */
package com.gym.app.domain.usecase.compra

import com.gym.app.domain.repository.RepositorioListaCompra
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class MarcarItemCompradoCasoUso
 * @brief Actualiza el estado de compra de un [com.gym.app.domain.model.ItemListaCompra]
 * dentro de una [com.gym.app.domain.model.ListaCompra], delegando en
 * [RepositorioListaCompra.marcarItemComprado].
 *
 * Este caso de uso permite al usuario marcar o desmarcar ítems mientras realiza
 * la compra en el supermercado, actualizando el estado sin regenerar la lista
 * completa.
 */
class MarcarItemCompradoCasoUso(
    private val repositorioListaCompra: RepositorioListaCompra,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Marca o desmarca un ítem de la lista de la compra como comprado.
     * @param listaId Identificador de la lista que contiene el ítem.
     * @param itemId Identificador del ítem a actualizar.
     * @param comprado Nuevo estado de compra (`true` = comprado).
     * @return [Result] con éxito (Unit) o con el error de persistencia.
     */
    suspend fun ejecutar(
        listaId: String,
        itemId: String,
        comprado: Boolean
    ): Result<Unit> = withContext(dispatcher) {
        try {
            repositorioListaCompra.marcarItemComprado(listaId, itemId, comprado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}