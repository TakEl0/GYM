/**
 * @file PantallaDashboardGYM.kt
 * @brief Panel de control (Dashboard) de la aplicación GYM en Jetpack Compose.
 * Muestra el resumen diario del usuario: saludo, rutina de hoy con progreso,
 * contadores semanales y accesos rápidos a Entrenamiento y Nutrición.
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.DashboardViewModel

/**
 * @brief Pantalla principal del panel de control de GYM.
 * Crea el ViewModel y muestra el estado reactivo con las distintas tarjetas
 * de resumen, manteniendo toda la lógica de negocio en la capa de dominio.
 */
@Composable
fun PantallaDashboardGYM() {
    val viewModel: DashboardViewModel = viewModel()
    val estado by viewModel.estado.collectAsStateWithLifecycle()

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
            CabeceraBienvenida()
            Spacer(modifier = Modifier.height(20.dp))
            TarjetaResumenDiario()
            Spacer(modifier = Modifier.height(16.dp))
            FilaMetricasSemana(estado.sesionesCompletadas, estado.totalSesionesSemana)
            Spacer(modifier = Modifier.height(16.dp))
            AccesosRapidos()
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
 * @brief Cabecera con el saludo y el nombre del usuario.
 */
@Composable
private fun CabeceraBienvenida() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, Alex",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Lunes, 16 de agosto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(AzulPrimario, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * @brief Tarjeta hero con el resumen diario y su progreso.
 * Utiliza un gradiente azul profundo y muestra un anillo de progreso.
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
                        text = "1.680 / 2.200 kcal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "76% del objetivo calórico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 0.76f },
                        modifier = Modifier.size(72.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeWidth = 7.dp
                    )
                    Text(
                        text = "76%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
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
 */
@Composable
private fun FilaMetricasSemana(sesionesCompletadas: Int, totalSesiones: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaMetrica(
            icono = Icons.Filled.Bolt,
            titulo = "Sesión hoy",
            valor = "Push A",
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
 */
@Composable
private fun AccesosRapidos() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAcceso(
            icono = Icons.Filled.FitnessCenter,
            titulo = "Entrenamiento",
            colorFondo = AzulPrimario,
            modifier = Modifier.weight(1f)
        )
        TarjetaAcceso(
            icono = Icons.Filled.Restaurant,
            titulo = "Nutrición",
            colorFondo = CianAcento,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * @brief Tarjeta de acceso rápido con fondo de color.
 * @param icono Icono del acceso.
 * @param titulo Nombre del acceso.
 * @param colorFondo Color de fondo de la tarjeta.
 * @param modifier Modificador de la tarjeta.
 */
@Composable
private fun TarjetaAcceso(
    icono: ImageVector,
    titulo: String,
    colorFondo: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
            Spacer(modifier = Modifier.height(8.dp))
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
                progress = { entrenamiento.progresoPorcentaje / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = AzulPrimario,
                trackColor = SuperficieElevada,
                drawStopIndicator = {}
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
 * @brief Tarjeta con las próximas comidas del plan del nutricionista.
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
            FilaComida(
                icono = Icons.Filled.FoodBank,
                nombre = "Desayuno",
                hora = "08:00",
                macros = "P 40g · C 55g · G 15g"
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilaComida(
                icono = Icons.Filled.Restaurant,
                nombre = "Comida",
                hora = "14:00",
                macros = "P 45g · C 70g · G 20g"
            )
        }
    }
}

/**
 * @brief Fila individual de una comida del plan.
 * @param icono Icono representativo de la comida.
 * @param nombre Nombre de la comida.
 * @param hora Hora prevista de la comida.
 * @param macros Resumen de macronutrientes de la comida.
 */
@Composable
private fun FilaComida(
    icono: ImageVector,
    nombre: String,
    hora: String,
    macros: String
) {
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
                imageVector = icono,
                contentDescription = nombre,
                tint = AzulSecundario,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = macros,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = hora,
            style = MaterialTheme.typography.titleMedium,
            color = AzulSecundario,
            fontWeight = FontWeight.Medium
        )
    }
}