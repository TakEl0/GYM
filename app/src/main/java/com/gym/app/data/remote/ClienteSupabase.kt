/**
 * @file ClienteSupabase.kt
 * @brief Singleton para la configuración y acceso al cliente de Supabase (v2.2.2).
 */
package com.gym.app.data.remote

import android.content.Context
import com.gym.app.BuildConfig
import com.gym.app.data.local.seguridad.GestorSesionCifrado
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * @object ClienteSupabase
 * @brief Gestiona la inicialización del cliente Supabase con autenticación cifrada (Auth),
 * PostgREST y Realtime, soportando estado de configuración pendiente si faltan credenciales.
 */
object ClienteSupabase {

    val estaConfigurado: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() &&
                BuildConfig.SUPABASE_URL != "https://TU_PROYECTO.supabase.co" &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY != "TU_ANON_KEY_PUBLICA"

    private var clienteInstance: SupabaseClient? = null

    /**
     * @brief Inicializa el cliente Supabase si las credenciales son válidas.
     * @param context Contexto de la aplicación.
     * @return [SupabaseClient] o null si no está configurado.
     */
    fun inicializar(context: Context): SupabaseClient? {
        if (!estaConfigurado) return null
        return clienteInstance ?: synchronized(this) {
            clienteInstance ?: createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth) {
                    sessionManager = GestorSesionCifrado(context)
                }
                install(Postgrest)
                install(Realtime)
            }.also {
                clienteInstance = it
            }
        }
    }

    /**
     * @brief Obtiene la instancia activa del cliente Supabase.
     * @throws IllegalStateException si no ha sido inicializado.
     */
    val cliente: SupabaseClient
        get() = clienteInstance ?: throw IllegalStateException("ClienteSupabase no ha sido inicializado o los secretos no están configurados.")
}
