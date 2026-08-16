package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DtoEventoCalendarioRemoto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val titulo: String,
    val descripcion: String?,
    @SerialName("fecha_inicio")
    val fechaInicio: Long,
    @SerialName("fecha_fin")
    val fechaFin: Long,
    val tipo: String
)
