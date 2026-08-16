/**
 * @file RepositorioGimnasioRoom.kt
 * @brief Implementación del repositorio de gimnasio con Room local.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.repository.RepositorioGimnasio
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioGimnasioRoom
 * @brief Administra el gimnasio del usuario y su maquinaria en Room local.
 */
class RepositorioGimnasioRoom(private val context: Context) : RepositorioGimnasio {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    private fun obtenerUserIdActual(): String {
        return try {
            val supabase = com.gym.app.data.remote.ClienteSupabase.inicializar(context)
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarGimnasio(): Flow<Gimnasio?> {
        val userId = obtenerUserIdActual()
        return db.daoGimnasio().observarGimnasio(userId).map { entidad ->
            entidad?.let { gym ->
                val maquinas = db.daoGimnasio().obtenerMaquinasDeGimnasio(gym.id)
                gym.aDominio(maquinas)
            }
        }
    }

    override suspend fun guardarGimnasio(gimnasio: Gimnasio) {
        val userId = obtenerUserIdActual()
        db.daoGimnasio().eliminarMaquinasDeGimnasio(gimnasio.id)
        db.daoGimnasio().insertarGimnasio(gimnasio.aEntidad(userId))
        for (maquina in gimnasio.maquinas) {
            db.daoGimnasio().insertarMaquina(maquina.aEntidad(gimnasio.id))
        }
    }

    override suspend fun actualizarDisponibilidadMaquina(maquinaId: String, disponible: Boolean) {
        db.daoGimnasio().actualizarDisponibilidadMaquina(maquinaId, disponible)
    }
}