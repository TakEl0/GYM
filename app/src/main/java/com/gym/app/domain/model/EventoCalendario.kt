package com.gym.app.domain.model

data class EventoCalendario(
    val id: String,
    val userId: String,
    val titulo: String,
    val descripcion: String?,
    val fechaInicio: Long,
    val fechaFin: Long,
    val tipo: String // 'ENTRENAMIENTO_GRUPAL', 'CLASE', 'META_PERSONAL'
)
