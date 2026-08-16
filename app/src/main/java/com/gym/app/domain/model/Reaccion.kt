package com.gym.app.domain.model

data class Reaccion(
    val id: String,
    val publicacionId: String,
    val userId: String,
    val tipoReaccion: String // 'LIKE', 'FIRE', 'MUSCLE', 'APPLAUSE'
)
