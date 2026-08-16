/**
 * @file RepositorioListaCompraRoom.kt
 * @brief Implementación del repositorio de lista de la compra con Room local.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.domain.model.ListaCompra
import com.gym.app.domain.repository.RepositorioListaCompra
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioListaCompraRoom
 * @brief Administra las listas de la compra semanales. Son locales por ahora;
 * la sincronización remota se añadirá cuando el esquema de Supabase lo soporte.
 */
class RepositorioListaCompraRoom(private val context: Context) : RepositorioListaCompra {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    private fun obtenerUserIdActual(): String {
        return try {
            val supabase = ClienteSupabase.inicializar(context)
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarListas(): Flow<List<ListaCompra>> {
        val userId = obtenerUserIdActual()
        return db.daoListaCompra().observarListas(userId).flatMapLatest { listas ->
            if (listas.isEmpty()) flowOf(emptyList())
            else combine(
                listas.map { lista ->
                    db.daoListaCompra().observarItemsDeLista(lista.id)
                        .map { items -> lista.aDominio(items) }
                }
            ) { combinados -> combinados.toList() }
        }
    }

    override suspend fun guardarLista(lista: ListaCompra) {
        val userId = obtenerUserIdActual()
        val inicioSemana = lista.semanaInicio.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        // Si ya existe una lista para la misma semana, se reemplaza completa.
        val existente = db.daoListaCompra().obtenerListas(userId)
            .firstOrNull { it.semanaInicio == inicioSemana }

        existente?.let {
            db.daoListaCompra().eliminarItemsDeLista(it.id)
            db.daoListaCompra().eliminarLista(it.id)
        }

        db.daoListaCompra().insertarLista(lista.aEntidad(userId))
        for (item in lista.items) {
            db.daoListaCompra().insertarItem(item.aEntidad(lista.id))
        }
    }

    override suspend fun marcarItemComprado(listaId: String, itemId: String, comprado: Boolean) {
        db.daoListaCompra().marcarItemComprado(itemId, comprado)
    }
}