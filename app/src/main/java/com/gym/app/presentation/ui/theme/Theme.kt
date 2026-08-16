/**
 * @file Theme.kt
 * @brief Tema Material 3 de la aplicación GYM en modo oscuro azulado.
 * Aplica la paleta de colores definida en Color.kt a un esquema de color
 * oscuro de Material 3 y establece la tipografía de la aplicación.
 */
package com.gym.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * @brief Esquema de color oscuro de Material 3 personalizado para GYM.
 * Mapea los roles semánticos de Material 3 a la paleta azul profunda,
 * garantizando contraste y coherencia visual en toda la aplicación.
 */
private val EsquemaColorGYM = darkColorScheme(
    primary = AzulPrimario,
    onPrimary = FondoOscuro,
    primaryContainer = SuperficieElevada,
    onPrimaryContainer = BlancoAzulado,
    secondary = AzulSecundario,
    onSecondary = FondoOscuro,
    secondaryContainer = SuperficieElevada,
    onSecondaryContainer = BlancoAzulado,
    tertiary = CianAcento,
    onTertiary = FondoOscuro,
    tertiaryContainer = Color(0xFF0F2E2A),
    onTertiaryContainer = CianAcento,
    background = FondoOscuro,
    onBackground = BlancoAzulado,
    surface = SuperficieOscura,
    onSurface = BlancoAzulado,
    surfaceVariant = SuperficieElevada,
    onSurfaceVariant = GrisAzulado,
    surfaceTint = AzulPrimario,
    outline = BordeAzulado,
    outlineVariant = BordeAzulado,
    error = ErrorOscuro,
    onError = FondoOscuro,
    errorContainer = Color(0xFF4A1E1E),
    onErrorContainer = ErrorOscuro
)

/**
 * @brief Aplica el tema oscuro azulado de GYM al contenido.
 * @param content Contenido composable envuelto por el tema.
 */
@Composable
fun TemaGYM(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColorGYM,
        typography = TipografiaGYM,
        content = content
    )
}