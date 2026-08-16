/**
 * @file RepositorioPerfilRoom.kt
 * @brief Implementación del repositorio de perfil con Room como fuente primaria.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioPerfilRoom
 * @brief Administra la persistencia local del perfil y sus objetivos nutricionales.
 * Los campos nuevos (altura, edad, sexo, factor de actividad y objetivo) solo se
 * guardan en Room; la tabla remota `perfiles` de Supabase no dispone todavía de
 * esas columnas, por lo que la sincronización remota cubre únicamente email,
 * nombre y peso objetivo.
 */
class RepositorioPerfilRoom(private val context: Context) : RepositorioPerfil {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    override fun observarPerfil(id: String): Flow<PerfilUsuario?> =
        db.daoPerfilUsuario().observarPerfil(id).map { it?.aDominio() }

    override suspend fun obtenerPerfil(id: String): PerfilUsuario? =
        db.daoPerfilUsuario().obtenerPerfil(id)?.aDominio()

    override suspend fun guardarPerfil(perfil: PerfilUsuario) {
        db.daoPerfilUsuario().insertar(perfil.aEntidad())
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
        db.daoPerfilUsuario().actualizarObjetivos(
            id = id,
            pesoObjetivoKg = pesoObjetivoKg,
            alturaCm = alturaCm,
            edad = edad,
            sexo = sexo,
            factorActividad = factorActividad,
            objetivo = objetivo
        )
    }
}