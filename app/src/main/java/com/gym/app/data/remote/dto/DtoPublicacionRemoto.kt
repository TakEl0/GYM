package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DtoPublicacionRemoto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("autor_nombre")
    val autorNombre: String,
    val contenido: String,
    @SerialName("url_imagen")
    val urlImagen: String?,
    val tipo: String,
    val fecha: Long
)
