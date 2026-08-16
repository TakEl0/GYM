/**
 * @file PantallaCalendarioEntrenosGYM.kt
 * @brief Calendario personal interactivo de entrenamientos en Jetpack Compose.
 * Muestra una rejilla mensual, marca los días con sesión programada, permite
 * navegar entre meses y detalla los entrenamientos del día seleccionado.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.CalendarioEntrenosViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Días de la semana comenzando en lunes (convención europea/castellana). */
private val DIAS_SEMANA = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)

/**
 * @brief Pantalla del calendario personal de entrenamientos.
 * Crea el ViewModel a partir del contenedor de dependencias, muestra la rejilla
 * mensual con los días marcados y detalla las sesiones del día seleccionado.
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun PantallaCalendarioEntrenosGYM(contenedor: ContenedorDependencias) {
    val viewModel: CalendarioEntrenosViewModel = viewModel { CalendarioEntrenosViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.mesVisible) {
        viewModel.cargarMes(estado.mesVisible)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Calendario de Entrenos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Planifica y consulta tus sesiones día a día",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CabeceraMes(
                        mes = estado.mesVisible,
                        onMesAnterior = { viewModel.cambiarMes(-1) },
                        onMesSiguiente = { viewModel.cambiarMes(1) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilaDiasSemana()
                    Spacer(modifier = Modifier.height(4.dp))
                    RejillaDias(
                        mes = estado.mesVisible,
                        diaSeleccionado = estado.diaSeleccionado,
                        fechasConEntreno = estado.entrenamientos
                            .map { it.fecha }
                            .filter { it > 0L }
                            .map { epoch ->
                                java.time.Instant.ofEpochMilli(epoch)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            .toSet(),
                        onDiaSeleccionado = viewModel::seleccionarDia
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Sesiones del día",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val dia = estado.diaSeleccionado
            val entrenosDelDia = dia?.let { viewModel.entrenamientosDelDia(it) }.orEmpty()
            if (dia == null) {
                Text(
                    text = "Selecciona un día para ver sus entrenamientos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (entrenosDelDia.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
                ) {
                    Text(
                        text = "Descanso: no hay entrenos programados para este día.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                entrenosDelDia.forEach { entrenamiento ->
                    TarjetaEntrenoDelDia(
                        nombre = entrenamiento.nombre,
                        grupoMuscular = entrenamiento.grupoMuscular.joinToString(" · "),
                        completo = entrenamiento.completo,
                        progreso = entrenamiento.progresoPorcentaje
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * @brief Cabecera con el mes y año y los botones de navegación.
 * @param mes Mes calendario visible.
 * @param onMesAnterior Acción al pulsar la flecha izquierda.
 * @param onMesSiguiente Acción al pulsar la flecha derecha.
 */
@Composable
private fun CabeceraMes(mes: YearMonth, onMesAnterior: () -> Unit, onMesSiguiente: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMesAnterior) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Mes anterior",
                tint = AzulSecundario
            )
        }
        Text(
            text = mes.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.titlecase() } + " " + mes.year,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onMesSiguiente) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Mes siguiente",
                tint = AzulSecundario
            )
        }
    }
}

/**
 * @brief Fila con las abreviaturas de los días de la semana (L, M, X...).
 */
@Composable
private fun FilaDiasSemana() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DIAS_SEMANA.forEach { dia ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dia.getDisplayName(TextStyle.NARROW, Locale.getDefault()).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * @brief Rejilla mensual de días con las sesiones marcadas y el día seleccionado.
 * Construye filas de 7 celdas (una por día de la semana) comenzando por lunes,
 * reservando celdas vacías hasta el primer día del mes.
 * @param mes Mes a dibujar.
 * @param diaSeleccionado Día seleccionado actualmente (o null).
 * @param fechasConEntreno Conjunto de días que contienen al menos un entrenamiento.
 * @param onDiaSeleccionado Callback al pulsar un día concreto.
 */
@Composable
private fun RejillaDias(
    mes: YearMonth,
    diaSeleccionado: LocalDate?,
    fechasConEntreno: Set<LocalDate>,
    onDiaSeleccionado: (LocalDate) -> Unit
) {
    val primerDia = mes.atDay(1)
    val celdasVacias = DIAS_SEMANA.indexOf(primerDia.dayOfWeek)
    val totalDias = mes.lengthOfMonth()

    // Construye la lista de celdas: huecos iniciales + un elemento por día.
    val celdas: List<Int?> = List(celdasVacias) { null } + (1..totalDias).map { it }

    // Agrupa las celdas en semanas de 7 elementos.
    celdas.chunked(7).forEach { semana ->
        Row(modifier = Modifier.fillMaxWidth()) {
            semana.forEach { dia ->
                if (dia == null) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    val fecha = mes.atDay(dia)
                    CeldaDia(
                        dia = dia,
                        tieneEntreno = fechasConEntreno.contains(fecha),
                        seleccionado = fecha == diaSeleccionado,
                        esHoy = fecha == LocalDate.now(),
                        onClick = { onDiaSeleccionado(fecha) }
                    )
                }
            }
            // Rellena la última semana incompleta para mantener 7 columnas.
            repeat(7 - semana.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * @brief Celda individual del calendario.
 * @param dia Número del día.
 * @param tieneEntreno Indica si hay sesión programada ese día.
 * @param seleccionado Indica si el día está seleccionado.
 * @param esHoy Indica si el día es la fecha actual.
 * @param onClick Acción al pulsar la celda.
 */
@Composable
private fun RowScope.CeldaDia(
    dia: Int,
    tieneEntreno: Boolean,
    seleccionado: Boolean,
    esHoy: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .background(
                    when {
                        seleccionado -> AzulPrimario
                        esHoy -> AzulSecundario.copy(alpha = 0.35f)
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dia.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (seleccionado || esHoy) FontWeight.Bold else FontWeight.Normal
                )
                if (tieneEntreno) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (seleccionado) Color.White else CianAcento,
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

/**
 * @brief Tarjeta con el detalle de un entrenamiento del día.
 * @param nombre Nombre de la sesión.
 * @param grupoMuscular Grupos musculares trabajados.
 * @param completo Indica si la sesión está completada.
 * @param progreso Porcentaje de progreso de la sesión.
 */
@Composable
private fun TarjetaEntrenoDelDia(
    nombre: String,
    grupoMuscular: String,
    completo: Boolean,
    progreso: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SuperficieElevada, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (completo) Icons.Filled.CheckCircle else Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = if (completo) CianAcento else AzulSecundario,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = grupoMuscular,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (completo) "Completada · 100%" else "En progreso · $progreso%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (completo) CianAcento else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}