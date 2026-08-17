/**
 * @file RepositorioMapeoAprendidoRoom.kt
 * @brief Implementación del repositorio de mapeos aprendidos con Room local.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.MapeoAprendido
import com.gym.app.domain.repository.RepositorioMapeoAprendido
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioMapeoAprendidoRoom
 * @brief Administra los mapeos aprendidos (correcciones manuales de ejercicios → máquinas)
 * en la base de datos local Room.
 *
 * El aprendizaje es un dato 100 % local y funciona sin conexión, por lo que no depende
 * de Supabase ni de ningún servicio remoto.
 */
class RepositorioMapeoAprendidoRoom(private val context: Context) : RepositorioMapeoAprendido {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    override suspend fun guardar(m: MapeoAprendido) {
        db.daoMapeoAprendido().insertar(m.aEntidad())
    }

    override suspend fun buscar(nombreNormalizado: String): MapeoAprendido? =
        db.daoMapeoAprendido().buscar(nombreNormalizado)?.aDominio()

    override fun observar(): Flow<List<MapeoAprendido>> =
        db.daoMapeoAprendido().observarTodos().map { lista ->
            lista.map { it.aDominio() }
        }
}