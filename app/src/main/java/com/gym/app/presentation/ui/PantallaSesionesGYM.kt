/**
 * @file PantallaSesionesGYM.kt
 * @brief Pantalla de Sesiones (historial semanal) de la aplicación GYM en Jetpack Compose.
 * Muestra el historial de sesiones de entrenamiento de la semana actual con un
 * contador semanal y permite registrar una sesión manual mediante un diálogo.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.SesionesViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * @brief Pantalla de Sesiones (historial semanal) de la aplicación GYM.
 *
 * Crea el [SesionesViewModel] a partir del [ContenedorDependencias] y muestra el
 * historial de sesiones de entrenamiento de la semana actual (fecha, rutina,
 * series, duración y estado completo/incompleto) junto con un contador semanal.
 * El botón flotante abre el diálogo de registro manual de una sesión.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun PantallaSesionesGYM(contenedor: ContenedorDependencias) {
    val viewModel: SesionesViewModel = viewModel { SesionesViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    var mostrarDialogoRegistro by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoRegistro = true },
                containerColor = AzulPrimario,
                contentColor = Color.White
            ) {
                if (estado.registrando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar sesión")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                CabeceraSesiones()
                Spacer(modifier = Modifier.height(16.dp))
                ContadorSemanal(
                    totalSesiones = estado.sesiones.size,
                    completadas = estado.sesionesCompletadas,
                    inicioSemana = estado.inicioSemana,
                    finSemana = estado.finSemana
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (estado.sesiones.isEmpty()) {
                if (!estado.cargando) {
                    EstadoVacioSesiones()
                }
            } else {
                ListaSesiones(sesiones = estado.sesiones)
            }

            if (!estado.error.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = estado.error!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (mostrarDialogoRegistro) {
        DialogoRegistrarSesion(
            onConfirmar = { nombre, series, duracion, completo ->
                viewModel.registrarSesion(nombre, series, duracion, completo)
                mostrarDialogoRegistro = false
            },
            onCancelar = { mostrarDialogoRegistro = false }
        )
    }
}

/**
 * @brief Cabecera de la pantalla de Sesiones.
 */
@Composable
private fun CabeceraSesiones() {
    Column {
        Text(
            text = "Sesiones",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tu historial de entrenos de la semana",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * @brief Contador semanal de sesiones con las fechas de la semana observada.
 * @param totalSesiones Sesiones registradas en la semana.
 * @param completadas Sesiones completadas en la semana.
 * @param inicioSemana Fecha de inicio (lunes) de la semana.
 * @param finSemana Fecha de fin (domingo) de la semana.
 */
@Composable
private fun ContadorSemanal(
    totalSesiones: Int,
    completadas: Int,
    inicioSemana: java.time.LocalDate,
    finSemana: java.time.LocalDate
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Semana del ${formatearFechaCorta(inicioSemana)} al " +
                        formatearFechaCorta(finSemana),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (totalSesiones == 0) {
                        "Sin sesiones esta semana"
                    } else {
                        "$totalSesiones sesiones · $completadas completadas"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SuperficieElevada, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = completadas.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = CianAcento,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * @brief Estado vacío mostrado cuando no hay sesiones en la semana.
 */
@Composable
private fun EstadoVacioSesiones() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SuperficieElevada, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = AzulPrimario,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay sesiones esta semana",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Registra tu primer entreno con el botón + para hacer " +
                    "seguimiento de tu volumen semanal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * @brief Lista de sesiones de la semana.
 * @param sesiones Sesiones de entrenamiento a representar.
 */
@Composable
private fun ListaSesiones(sesiones: List<SesionEntrenamiento>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(sesiones, key = { it.id }) { sesion ->
            FilaSesion(sesion = sesion)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * @brief Fila con los datos de una sesión de entrenamiento.
 * @param sesion Sesión a representar.
 */
@Composable
private fun FilaSesion(sesion: SesionEntrenamiento) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    imageVector = if (sesion.completo) Icons.Filled.CheckCircle else Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = if (sesion.completo) CianAcento else AzulSecundario,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sesion.nombreRutina,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatearFechaSesion(sesion.fecha),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = AzulSecundario,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${sesion.serieRealizadas} series",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = AzulSecundario,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${sesion.duracionMinutos} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (sesion.completo) "Completa" else "Incompleta",
                style = MaterialTheme.typography.labelMedium,
                color = if (sesion.completo) CianAcento else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * @brief Diálogo de registro manual de una sesión de entrenamiento.
 * @param onConfirmar Acción de confirmación con los datos de la sesión.
 * @param onCancelar Acción de cancelación del diálogo.
 */
@Composable
private fun DialogoRegistrarSesion(
    onConfirmar: (nombre: String, series: Int, duracion: Int, completo: Boolean) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var completo by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Registrar sesión") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la rutina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = series,
                    onValueChange = { series = it.filter { caracter -> caracter.isDigit() } },
                    label = { Text("Series realizadas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = duracion,
                    onValueChange = { duracion = it.filter { caracter -> caracter.isDigit() } },
                    label = { Text("Duración (minutos)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = completo,
                        onCheckedChange = { completo = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CianAcento,
                            uncheckedColor = AzulSecundario
                        )
                    )
                    Text(
                        text = "Sesión completada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmar(
                        nombre,
                        series.toIntOrNull() ?: 0,
                        duracion.toIntOrNull() ?: 0,
                        completo
                    )
                },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = AzulSecundario) }
        }
    )
}

/**
 * @brief Formatea la fecha de una sesión (epoch millis) en castellano.
 * @param epochMillis Fecha de la sesión en milisegundos desde la época.
 * @return Fecha formateada (p. ej. "lunes, 16 de agosto").
 */
private fun formatearFechaSesion(epochMillis: Long): String {
    val fecha = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val formateador = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val texto = fecha.format(formateador)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
}

/**
 * @brief Formatea una fecha en formato corto (p. ej. "16/08").
 * @param fecha Fecha a formatear.
 * @return Fecha en formato "dd/MM".
 */
private fun formatearFechaCorta(fecha: java.time.LocalDate): String {
    val formateador = DateTimeFormatter.ofPattern("dd/MM", Locale("es", "ES"))
    return fecha.format(formateador)
}