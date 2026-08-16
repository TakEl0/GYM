/**
 * @file DaoListaCompra.kt
 * @brief DAO para la gestión de listas de la compra y sus artículos.
 */
package com.gym.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.app.data.local.entidad.EntidadItemListaCompra
import com.gym.app.data.local.entidad.EntidadListaCompra
import kotlinx.coroutines.flow.Flow

/**
 * @interface DaoListaCompra
 * @brief Operaciones de base de datos para las listas de la compra semanales.
 */
@Dao
interface DaoListaCompra {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(lista: EntidadListaCompra)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarItem(item: EntidadItemListaCompra)

    @Query("SELECT * FROM lista_compra WHERE userId = :userId")
    fun observarListas(userId: String): Flow<List<EntidadListaCompra>>

    @Query("SELECT * FROM lista_compra WHERE userId = :userId")
    suspend fun obtenerListas(userId: String): List<EntidadListaCompra>

    @Query("SELECT * FROM item_lista_compra WHERE listaId = :listaId")
    fun observarItemsDeLista(listaId: String): Flow<List<EntidadItemListaCompra>>

    @Query("SELECT * FROM item_lista_compra WHERE listaId = :listaId")
    suspend fun obtenerItemsDeLista(listaId: String): List<EntidadItemListaCompra>

    @Query("DELETE FROM lista_compra WHERE id = :id")
    suspend fun eliminarLista(id: String)

    @Query("DELETE FROM item_lista_compra WHERE listaId = :listaId")
    suspend fun eliminarItemsDeLista(listaId: String)

    @Query("UPDATE item_lista_compra SET comprado = :comprado WHERE id = :itemId")
    suspend fun marcarItemComprado(itemId: String, comprado: Boolean)
}