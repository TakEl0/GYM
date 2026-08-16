/**
 * @file RepositorioRutinaRoom.kt
 * @brief Implementación del repositorio de rutinas con Room local.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.repository.RepositorioRutina
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioRutinaRoom
 * @brief Administra las rutinas de entrenamiento del usuario en Room local.
 */
class RepositorioRutinaRoom(private val context: Context) : RepositorioRutina {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    private fun obtenerUserIdActual(): String {
        return try {
            val supabase = com.gym.app.data.remote.ClienteSupabase.inicializar(context)
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarRutinas(): Flow<List<Rutina>> {
        val userId = obtenerUserIdActual()
        return db.daoRutina().observarRutinas(userId).map { rutinas ->
            rutinas.map { rutina ->
                val bloques = db.daoRutina().obtenerBloquesDeRutina(rutina.id)
                rutina.aDominio(bloques)
            }
        }
    }

    override suspend fun guardarRutina(rutina: Rutina) {
        val userId = obtenerUserIdActual()
        db.daoRutina().eliminarBloquesDeRutina(rutina.id)
        db.daoRutina().insertarRutina(rutina.aEntidad(userId))
        for (bloque in rutina.bloques) {
            db.daoRutina().insertarBloque(bloque.aEntidad(rutina.id))
        }
    }

    override suspend fun eliminarRutina(id: String) {
        db.daoRutina().eliminarBloquesDeRutina(id)
        db.daoRutina().eliminarRutina(id)
    }
}