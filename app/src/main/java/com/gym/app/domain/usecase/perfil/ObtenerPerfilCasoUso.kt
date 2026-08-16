/**
 * @file ObtenerPerfilCasoUso.kt
 * @brief Caso de uso de observación reactiva del perfil de usuario.
 */
package com.gym.app.domain.usecase.perfil

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import kotlinx.coroutines.flow.Flow

/**
 * @class ObtenerPerfilCasoUso
 * @brief Expone un flujo reactivo con el [PerfilUsuario] del usuario, permitiendo
 * que la capa de presentación se actualice automáticamente ante cualquier cambio
 * en los datos antropométricos o de objetivos.
 *
 * Delega directamente en [RepositorioPerfil.observarPerfil], manteniendo el caso
 * de uso como un pasamuros que oculta la fuente de datos concreta (Room, Supabase
 * o almacenamiento cifrado).
 */
class ObtenerPerfilCasoUso(
    private val repositorioPerfil: RepositorioPerfil
) {

    /**
     * @brief Observa el perfil del usuario por su identificador.
     * @param id Identificador único del perfil (coincide con el id del usuario autenticado).
     * @return [Flow] que emite el [PerfilUsuario], o `null` si aún no existe,
     * actualizándose ante cambios en la fuente de datos.
     */
    fun ejecutar(id: String): Flow<PerfilUsuario?> =
        repositorioPerfil.observarPerfil(id)
}