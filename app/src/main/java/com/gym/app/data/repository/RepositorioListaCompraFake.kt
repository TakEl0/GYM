/**
 * @file RepositorioListaCompraFake.kt
 * @brief Implementación simulada del repositorio de lista de la compra.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.ListaCompra
import com.gym.app.domain.repository.RepositorioListaCompra
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @class RepositorioListaCompraFake
 * @brief Repositorio de listas de la compra en memoria para desarrollo y tests.
 */
class RepositorioListaCompraFake : RepositorioListaCompra {

    private val listas = MutableStateFlow<List<ListaCompra>>(emptyList())

    override fun observarListas(): Flow<List<ListaCompra>> = listas

    override suspend fun guardarLista(lista: ListaCompra) {
        listas.value = listas.value.filterNot { it.id == lista.id } + lista
    }

    override suspend fun marcarItemComprado(listaId: String, itemId: String, comprado: Boolean) {
        listas.value = listas.value.map { lista ->
            if (lista.id != listaId) lista
            else lista.copy(items = lista.items.map { item ->
                if (item.id == itemId) item.copy(comprado = comprado) else item
            })
        }
    }
}