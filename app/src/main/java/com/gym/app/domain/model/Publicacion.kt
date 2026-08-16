package com.gym.app.domain.model

data class Publicacion(
    val id: String,
    val userId: String,
    val autorNombre: String,
    val contenido: String,
    val urlImagen: String?,
    val tipo: String, // 'ENTRENAMIENTO', 'MENSAJE', 'LOGRO'
    val fecha: Long,
    val reacciones: List<Reaccion> = emptyList()
)
