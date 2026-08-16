/**
 * @file RepositorioGimnasio.kt
 * @brief Puerto de repositorio del gimnasio en la capa de dominio.
 * Define el contrato para observar y mantener el catálogo de máquinas del gimnasio
 * del usuario, incluyendo su disponibilidad para sugerir sustituciones de ejercicios.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.Gimnasio
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioGimnasio
 * @brief Contrato de acceso a los datos del gimnasio del usuario.
 */
interface RepositorioGimnasio {

    /**
     * @brief Observa de forma reactiva el gimnasio del usuario.
     * @return Flujo reactivo con el [Gimnasio], o `null` si aún no está configurado.
     */
    fun observarGimnasio(): Flow<Gimnasio?>

    /**
     * @brief Guarda o actualiza la información completa del gimnasio.
     * @param gimnasio Gimnasio a persistir.
     */
    suspend fun guardarGimnasio(gimnasio: Gimnasio)

    /**
     * @brief Actualiza el estado de disponibilidad de una máquina concreta.
     * Se utiliza cuando una máquina está en reparación u ocupada y el motor de
     * sustituciones debe ofrecer alternativas.
     * @param maquinaId Identificador de la máquina a actualizar.
     * @param disponible Nuevo estado de disponibilidad.
     */
    suspend fun actualizarDisponibilidadMaquina(
        maquinaId: String,
        disponible: Boolean
    )
}