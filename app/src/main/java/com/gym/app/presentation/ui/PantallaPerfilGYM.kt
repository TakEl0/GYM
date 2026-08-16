/**
 * @file PantallaPerfilGYM.kt
 * @brief Pantalla de perfil de usuario de la aplicación GYM en Jetpack Compose.
 * Muestra un resumen básico del perfil (nombre derivado del correo y correo de
 * la sesión activa) y ofrece el botón de "Cerrar sesión" que invoca al caso de
 * uso `CerrarSesionCasoUso`. El nombre completo del usuario llegará cuando el
 * perfil sincronizado esté disponible; mientras tanto se emplea el prefijo del
 * correo como marcador temporal.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gym.app.di.ContenedorDependencias
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.SuperficieOscura
import kotlinx.coroutines.launch

/**
 * @brief Pantalla de perfil de usuario de GYM.
 * Muestra el nombre derivado del correo (marcador temporal hasta que el perfil
 * completo se sincronice), el correo asociado a la sesión activa (si está
 * disponible) y el botón de cierre de sesión. Al pulsarlo se invoca
 * `CerrarSesionCasoUso` y, una vez cerrada la sesión, el flujo reactivo de
 * `EstadoSesion` hace que la navegación principal regrese automáticamente a la
 * pantalla de autenticación.
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun PantallaPerfilGYM(contenedor: ContenedorDependencias) {
    val alcance = rememberCoroutineScope()
    var cerrandoSesion by remember { mutableStateOf(false) }
    val correoSesion = remember {
        contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.email
    }
    // Nombre de pila derivado del correo como marcador temporal mientras el
    // perfil completo no está sincronizado desde el backend.
    val nombreSesion = remember(correoSesion) { derivarNombreDesdeEmail(correoSesion) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tu cuenta y sesión en GYM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            TarjetaDatosUsuario(
                correoSesion = correoSesion,
                nombreUsuario = nombreSesion
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    cerrandoSesion = true
                    alcance.launch {
                        contenedor.cerrarSesionCasoUso.ejecutar()
                    }
                },
                enabled = !cerrandoSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cerrandoSesion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Tarjeta con los datos básicos del usuario autenticado.
 * El nombre mostrado es un marcador temporal derivado del correo; el nombre
 * completo llegará cuando el perfil sincronizado esté disponible.
 * @param correoSesion Correo electrónico de la sesión activa (puede ser null).
 * @param nombreUsuario Nombre derivado del correo (puede ser null).
 */
@Composable
private fun TarjetaDatosUsuario(correoSesion: String?, nombreUsuario: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(AzulPrimario, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Avatar del usuario",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(
                    text = nombreUsuario ?: "Usuario GYM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = correoSesion ?: "Sesión iniciada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzulSecundario
                )
            }
        }
    }
}

/**
 * @brief Deriva un nombre de pila a partir del correo de la sesión activa.
 * Se emplea como marcador temporal mientras el perfil completo se sincroniza
 * desde el backend; si el correo no está disponible devuelve null.
 * @param email Correo electrónico de la sesión (puede ser null).
 * @return Nombre con la primera letra en mayúscula, o null si no hay correo.
 */
private fun derivarNombreDesdeEmail(email: String?): String? {
    if (email.isNullOrBlank()) return null
    val prefijo = email.substringBefore('@').trim()
    if (prefijo.isEmpty()) return null
    return prefijo.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
}