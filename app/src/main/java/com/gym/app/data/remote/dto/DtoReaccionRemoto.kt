package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DtoReaccionRemoto(
    val id: String,
    @SerialName("publicacion_id")
    val publicacionId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("tipo_reaccion")
    val tipoReaccion: String
)
