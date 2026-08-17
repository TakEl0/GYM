/**
 * @file PantallaDashboardGYM.kt
 * @brief Panel de control (Dashboard) de la aplicación GYM en Jetpack Compose.
 * Muestra el saludo con la fecha real, el resumen diario, los contadores
 * semanales y los accesos rápidos a Entrenamiento y Nutrición. Todos los datos
 * provienen del estado reactivo del ViewModel o de la sesión real; no se
 * muestran datos inventados ni fijos.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * @brief Pantalla principal del panel de control de GYM.
 * Crea el ViewModel (inyectando el repositorio real desde el contenedor de
 * dependencias) y muestra el estado reactivo con las distintas tarjetas
 * de resumen, manteniendo toda la lógica de negocio en la capa de dominio.
 * El saludo se muestra de inmediato ("¡Hola!") sin bloquear la interfaz; si la
 * sesión activa expone un correo, se deriva el nombre del usuario como
 * marcador temporal hasta que el perfil completo esté sincronizado.
 * @param contenedor Contenedor de dependencias de la aplicación.
 * @param alNavegar Acción de navegación por ruta para los accesos rápidos
 * (p. ej. "nutricion", "lista_compra", "gimnasio", "rutinas" o "sesiones").
 * Por defecto no hace nada.
 */
@Composable
fun PantallaDashboardGYM(
    contenedor: ContenedorDependencias,
    alNavegar: (String) -> Unit = {}
) {
    val viewModel: DashboardViewModel = viewModel { DashboardViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Correo e identificador de la sesión activa (si existe).
    val sesionActiva = remember {
        contenedor.obtenerSesionActualCasoUso.ejecutar()
    }
    val correoSesion = sesionActiva?.user?.email
    val usuarioId = sesionActiva?.user?.id

    // Nombre real del usuario observado de forma reactiva desde el perfil
    // completo (poblado al autenticarse). Si el perfil aún no está disponible,
    // se usa como último recurso el nombre derivado del correo.
    val flujoPerfil = remember(usuarioId) {
        if (usuarioId.isNullOrBlank()) {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            contenedor.obtenerPerfilCasoUso.ejecutar(usuarioId)
        }
    }
    val perfilUsuario by flujoPerfil.collectAsStateWithLifecycle(initialValue = null)
    val nombreSesion = remember(perfilUsuario, correoSesion) {
        perfilUsuario?.nombre?.takeIf { it.isNotBlank() }
            ?: derivarNombreDesdeEmail(correoSesion)
    }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            CabeceraBienvenida(nombreUsuario = nombreSesion)
            Spacer(modifier = Modifier.height(20.dp))
            TarjetaResumenDiario()
            Spacer(modifier = Modifier.height(16.dp))
            FilaMetricasSemana(
                sesionesCompletadas = estado.sesionesCompletadas,
                totalSesiones = estado.totalSesionesSemana,
                entrenamientoDeHoy = estado.entrenamientoDeHoy
            )
            Spacer(modifier = Modifier.height(16.dp))
            AccesosRapidos(onNavegar = alNavegar)
            Spacer(modifier = Modifier.height(12.dp))
            AccesosAvanzados(onNavegar = alNavegar)
            Spacer(modifier = Modifier.height(16.dp))
            estado.entrenamientoDeHoy?.let { entrenamiento ->
                TarjetaProximaRutina(entrenamiento)
                Spacer(modifier = Modifier.height(16.dp))
            }
            TarjetaProximasComidas()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Cabecera con el saludo genérico, la fecha real de hoy y, si está
 * disponible, el nombre del usuario derivado del correo de la sesión.
 * Muestra siempre "¡Hola!" de forma inmediata; el avatar circular solo aparece
 * cuando existe un nombre derivado que representar con su inicial.
 * @param nombreUsuario Nombre derivado de la sesión (puede ser null).
 */
@Composable
private fun CabeceraBienvenida(nombreUsuario: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (nombreUsuario.isNullOrBlank()) {
                    "¡Hola!"
                } else {
                    "¡Hola! $nombreUsuario"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatearFechaActual(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!nombreUsuario.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AzulPrimario, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombreUsuario.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * @brief Tarjeta hero con el resumen diario.
 * Utiliza un gradiente azul profundo y muestra un estado vacío elegante mientras
 * no existan datos reales de nutrición (el ViewModel de nutrición se conectará
 * en una oleada posterior). No se muestran kilocalorías ni porcentajes fijos.
 */
@Composable
private fun TarjetaResumenDiario() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(AzulPrimario, AzulSecundario)
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resumen de hoy",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sin datos de nutrición todavía",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Registra tus comidas o importa tu dieta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * @brief Fila con las métricas de la semana.
 * @param sesionesCompletadas Sesiones completadas esta semana.
 * @param totalSesiones Total de sesiones planificadas.
 * @param entrenamientoDeHoy Rutina programada para hoy (puede ser null).
 */
@Composable
private fun FilaMetricasSemana(
    sesionesCompletadas: Int,
    totalSesiones: Int,
    entrenamientoDeHoy: Entrenamiento?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaMetrica(
            icono = Icons.Filled.Bolt,
            titulo = "Sesión hoy",
            valor = entrenamientoDeHoy?.nombre ?: "Sin sesión",
            colorIcono = CianAcento,
            modifier = Modifier.weight(1f)
        )
        TarjetaMetrica(
            icono = Icons.Filled.TrendingUp,
            titulo = "Esta semana",
            valor = "$sesionesCompletadas/$totalSesiones",
            colorIcono = AzulSecundario,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * @brief Tarjeta de métrica individual.
 * @param icono Icono representativo de la métrica.
 * @param titulo Título de la métrica.
 * @param valor Valor principal de la métrica.
 * @param colorIcono Color del icono de la métrica.
 * @param modifier Modificador de la tarjeta.
 */
@Composable
private fun TarjetaMetrica(
    icono: ImageVector,
    titulo: String,
    valor: String,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = colorIcono,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * @brief Atajos rápidos a las secciones de Entrenamiento y Nutrición.
 * @param onNavegar Acción de navegación por ruta.
 */
@Composable
private fun AccesosRapidos(onNavegar: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAcceso(
            icono = Icons.Filled.FitnessCenter,
            titulo = "Entrenamiento",
            colorFondo = AzulPrimario,
            onClick = { onNavegar(RutasSegundoNivelGYM.RUTINAS) },
            modifier = Modifier.weight(1f)
        )
        TarjetaAcceso(
            icono = Icons.Filled.Restaurant,
            titulo = "Nutrición",
            colorFondo = CianAcento,
            onClick = { onNavegar(DestinoGYM.NUTRICION.ruta) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * @brief Accesos avanzados a las pantallas de segundo nivel: Lista de la compra,
 * Gimnasio, Rutinas e Historial de sesiones.
 * @param onNavegar Acción de navegación por ruta.
 */
@Composable
private fun AccesosAvanzados(onNavegar: (String) -> Unit) {
    Text(
        text = "Herramientas",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAcceso(
            icono = Icons.Filled.ShoppingCart,
            titulo = "Lista de la compra",
            colorFondo = SuperficieOscura,
            onClick = { onNavegar(RutasSegundoNivelGYM.LISTA_COMPRA) },
            modifier = Modifier.weight(1f)
        )
        TarjetaAcceso(
            icono = Icons.Filled.FitnessCenter,
            titulo = "Gimnasio",
            colorFondo = SuperficieOscura,
            onClick = { onNavegar(RutasSegundoNivelGYM.GIMNASIO) },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAcceso(
            icono = Icons.Filled.FormatListBulleted,
            titulo = "Rutinas",
            colorFondo = SuperficieOscura,
            onClick = { onNavegar(RutasSegundoNivelGYM.RUTINAS) },
            modifier = Modifier.weight(1f)
        )
        TarjetaAcceso(
            icono = Icons.Filled.History,
            titulo = "Historial de sesiones",
            colorFondo = SuperficieOscura,
            onClick = { onNavegar(RutasSegundoNivelGYM.SESIONES) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * @brief Tarjeta de acceso rápido con fondo de color y acción de navegación.
 * @param icono Icono del acceso.
 * @param titulo Nombre del acceso.
 * @param colorFondo Color de fondo de la tarjeta.
 * @param onClick Acción al pulsar la tarjeta.
 * @param modifier Modificador de la tarjeta.
 */
@Composable
private fun TarjetaAcceso(
    icono: ImageVector,
    titulo: String,
    colorFondo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * @brief Tarjeta con la próxima rutina y su barra de progreso.
 * @param entrenamiento Rutina de entrenamiento a mostrar.
 */
@Composable
private fun TarjetaProximaRutina(entrenamiento: Entrenamiento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Próxima rutina",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entrenamiento.nombre,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entrenamiento.grupoMuscular.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = entrenamiento.progresoPorcentaje / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = AzulPrimario,
                trackColor = SuperficieElevada
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${entrenamiento.ejerciciosRealizados}/${entrenamiento.totalEjercicios} ejercicios · ${entrenamiento.progresoPorcentaje}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * @brief Tarjeta con las comidas del día.
 * Muestra un estado vacío elegante mientras el ViewModel de nutrición no esté
 * conectado: no se muestran comidas ni macronutrientes inventados.
 */
@Composable
private fun TarjetaProximasComidas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Próximas comidas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SuperficieElevada, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = AzulSecundario,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sin comidas registradas hoy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tu plan de comidas aparecerá aquí",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

/**
 * @brief Formatea la fecha actual en español (p. ej. "lunes, 16 de agosto").
 * Utiliza `java.time` con locale de España; el desugaring del proyecto permite
 * usarlo en versiones antiguas de Android (minSdk 24).
 * @return Fecha actual formateada en español con la primera letra en mayúscula.
 */
private fun formatearFechaActual(): String {
    val fecha = LocalDate.now()
    val formateador = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val texto = fecha.format(formateador)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
}