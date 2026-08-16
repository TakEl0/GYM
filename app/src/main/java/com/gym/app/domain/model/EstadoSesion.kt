/**
 * @file EstadoSesion.kt
 * @brief Define los posibles estados de autenticación del usuario en la sesión.
 */
package com.gym.app.domain.model

/**
 * @enum EstadoSesion
 * @brief Representa el estado actual de la sesión de usuario en la aplicación.
 */
enum class EstadoSesion {
    NO_AUTENTICADO,
    AUTENTICADO,
    CARGANDO,
    CONFIGURACION_PENDIENTE
}
