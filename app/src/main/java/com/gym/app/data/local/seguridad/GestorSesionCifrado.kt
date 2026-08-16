/**
 * @file GestorSesionCifrado.kt
 * @brief Implementación segura de SessionManager utilizando Android Keystore y EncryptedSharedPreferences.
 */
package com.gym.app.data.local.seguridad

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * @class GestorSesionCifrado
 * @brief Administrador seguro de sesiones GoTrue para Supabase.
 * Cifra y descifra las credenciales y tokens de sesión mediante AES256_GCM y Android Keystore,
 * evitando exposiciones en texto plano.
 */
class GestorSesionCifrado(private val context: Context) : SessionManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val sharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "supabase_session_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveSession(session: UserSession) {
        val sessionJson = json.encodeToString(session)
        sharedPreferences.edit().putString("supabase_user_session", sessionJson).apply()
    }

    override suspend fun loadSession(): UserSession? {
        val sessionJson = sharedPreferences.getString("supabase_user_session", null) ?: return null
        return try {
            json.decodeFromString<UserSession>(sessionJson)
        } catch (e: Exception) {
            deleteSession()
            null
        }
    }

    override suspend fun deleteSession() {
        sharedPreferences.edit().remove("supabase_user_session").apply()
    }
}
