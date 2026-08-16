/**
 * @file RepositorioPerfilFake.kt
 * @brief Implementación simulada del repositorio de perfil para tests y desarrollo.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @class RepositorioPerfilFake
 * @brief Repositorio de perfil en memoria con datos de ejemplo.
 */
class RepositorioPerfilFake : RepositorioPerfil {

    private val perfil = MutableStateFlow(
        PerfilUsuario(
            id = "user_fake",
            email = "usuario@ejemplo.com",
            nombre = "Usuario GYM",
            pesoObjetivoKg = 80.0,
            alturaCm = 178.0,
            edad = 30,
            sexo = "MASCULINO",
            factorActividad = "MODERADO",
            objetivo = "DEFINICION"
        )
    )

    override fun observarPerfil(id: String): Flow<PerfilUsuario?> = perfil

    override suspend fun obtenerPerfil(id: String): PerfilUsuario? = perfil.value

    override suspend fun guardarPerfil(perfil: PerfilUsuario) {
        this.perfil.value = perfil
    }

    override suspend fun actualizarObjetivos(
        id: String,
        pesoObjetivoKg: Double,
        alturaCm: Double,
        edad: Int,
        sexo: String,
        factorActividad: String,
        objetivo: String
    ) {
        this.perfil.value = perfil.value.copy(
            pesoObjetivoKg = pesoObjetivoKg,
            alturaCm = alturaCm,
            edad = edad,
            sexo = sexo,
            factorActividad = factorActividad,
            objetivo = objetivo
        )
    }
}