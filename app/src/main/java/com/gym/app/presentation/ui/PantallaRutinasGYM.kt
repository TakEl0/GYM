/**
 * @file PantallaRutinasGYM.kt
 * @brief Pantalla de Rutinas de la aplicación GYM en Jetpack Compose.
 * Muestra las rutinas configuradas con sus bloques de series y los días de la
 * semana en los que se ejecutan, permite construir automáticamente una rutina
 * PPL para el día elegido y ofrece una calculadora de 1RM (Epley/Brzycki).
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.gym.app.domain.model.BloqueRutina
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Rutina
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.RutinasViewModel
import kotlin.math.roundToInt

/** Días de la semana para la construcción de rutinas (1 = lunes ... 7 = domingo). */
private val DIAS_CONSTRUCCION: List<Pair<Int, String>> = listOf(
    1 to "Lunes",
    2 to "Martes",
    3 to "Miércoles",
    4 to "Jueves",
    5 to "Viernes",
    6 to "Sábado"
)

/**
 * @brief Pantalla de Rutinas de la aplicación GYM.
 *
 * Crea el [RutinasViewModel] a partir del [ContenedorDependencias] y muestra la
 * lista de rutinas configuradas con sus bloques y días de la semana, una
 * calculadora de 1RM y el botón flotante que abre el diálogo de construcción
 * automática de una rutina PPL para el día seleccionado. Cada tarjeta de rutina
 * incluye el botón "Entrenar" que inicia la sesión en vivo.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 * @param alNavegar Acción de navegación a otra pantalla con la ruta destino.
 */
@Composable
fun PantallaRutinasGYM(
    contenedor: ContenedorDependencias,
    alNavegar: (String) -> Unit = {}
) {
    val viewModel: RutinasViewModel = viewModel { RutinasViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    var mostrarDialogoConstruccion by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoConstruccion = true },
                containerColor = AzulPrimario,
                contentColor = Color.White
            ) {
                if (estado.construyendo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir rutina")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            CabeceraRutinas()
            Spacer(modifier = Modifier.height(16.dp))

            CalculadoraUnRM(
                onCalcular = viewModel::calcularUnRM,
                resultado = estado.calculoUnRM
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mis rutinas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (estado.rutinas.isEmpty()) {
                EstadoVacioRutinas()
            } else {
                estado.rutinas.forEach { rutina ->
                    TarjetaRutina(
                        rutina = rutina,
                        ejercicios = estado.ejercicios,
                        onEntrenar = { id ->
                            alNavegar("${RutasSegundoNivelGYM.SESION_ACTIVA}/$id")
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (!estado.error.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarDialogoConstruccion) {
        DialogoConstruirRutina(
            onConfirmar = { dia ->
                viewModel.construirRutina(dia)
                mostrarDialogoConstruccion = false
            },
            onCancelar = { mostrarDialogoConstruccion = false }
        )
    }
}

/**
 * @brief Cabecera de la pantalla de Rutinas.
 */
@Composable
private fun CabeceraRutinas() {
    Column {
        Text(
            text = "Rutinas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Programación PPL y fuerza máxima",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * @brief Calculadora de 1RM con campos de peso y repeticiones.
 * @param onCalcular Acción de cálculo con el peso y las repeticiones.
 * @param resultado Estimación del 1RM calculada (null si aún no hay resultado).
 */
@Composable
private fun CalculadoraUnRM(
    onCalcular: (pesoKg: Double, repeticiones: Int) -> Unit,
    resultado: Double?
) {
    var peso by remember { mutableStateOf("") }
    var repeticiones by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = AzulPrimario,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Calculadora de 1RM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Promedio de las fórmulas de Epley y Brzycki",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it.filter { caracter -> caracter.isDigit() || caracter == '.' } },
                    label = { Text("Peso (kg)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = repeticiones,
                    onValueChange = { repeticiones = it.filter { caracter -> caracter.isDigit() } },
                    label = { Text("Repeticiones") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val pesoValor = peso.toDoubleOrNull() ?: 0.0
                    val repeticionesValor = repeticiones.toIntOrNull() ?: 0
                    onCalcular(pesoValor, repeticionesValor)
                },
                enabled = peso.toDoubleOrNull()?.let { it > 0.0 } == true &&
                    repeticiones.toIntOrNull()?.let { it in 1..35 } == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Calcular 1RM",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            resultado?.let { valor ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuperficieElevada, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tu 1RM estimado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(valor * 10).roundToInt() / 10.0} kg",
                        style = MaterialTheme.typography.titleLarge,
                        color = CianAcento,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * @brief Estado vacío mostrado cuando no hay rutinas configuradas.
 */
@Composable
private fun EstadoVacioRutinas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = AzulPrimario,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Todavía no tienes rutinas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pulsa el botón + y elige el día para construir tu rutina PPL.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * @brief Tarjeta con una rutina, sus bloques de series y los días de la semana.
 * @param rutina Rutina a representar.
 * @param ejercicios Catálogo de ejercicios para resolver los nombres de los bloques.
 * @param onEntrenar Acción al pulsar "Entrenar" con el id de la rutina.
 */
@Composable
private fun TarjetaRutina(
    rutina: Rutina,
    ejercicios: List<Ejercicio>,
    onEntrenar: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rutina.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    rutina.descripcion?.let { descripcion ->
                        Text(
                            text = descripcion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${rutina.bloques.size} bloques",
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulSecundario
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilaDiasRutina(diasSemana = rutina.diasSemana)
            Spacer(modifier = Modifier.height(10.dp))
            // Los bloques se agrupan por ejercicio para mostrar series y repeticiones.
            val bloquesPorEjercicio = rutina.bloques.groupBy { it.ejercicioId }
            bloquesPorEjercicio.forEach { (ejercicioId, bloques) ->
                val nombreEjercicio = ejercicios.firstOrNull { it.id == ejercicioId }?.nombre
                    ?: "Ejercicio del catálogo"
                FilaBloqueEjercicio(
                    nombreEjercicio = nombreEjercicio,
                    bloques = bloques
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { onEntrenar(rutina.id) },
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Entrenar",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * @brief Fila de los días de la semana en los que se ejecuta una rutina.
 * @param diasSemana Días de la semana (1 = lunes ... 7 = domingo).
 */
@Composable
private fun FilaDiasRutina(diasSemana: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val letras = listOf("L", "M", "X", "J", "V", "S", "D")
        letras.forEachIndexed { indice, letra ->
            val dia = indice + 1
            val activo = diasSemana.contains(dia)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (activo) AzulPrimario else SuperficieElevada,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (activo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * @brief Fila con el nombre de un ejercicio y el resumen de sus series.
 * @param nombreEjercicio Nombre del ejercicio (resuelto desde el catálogo).
 * @param bloques Bloques de series del ejercicio.
 */
@Composable
private fun FilaBloqueEjercicio(
    nombreEjercicio: String,
    bloques: List<BloqueRutina>
) {
    val primerBloque = bloques.firstOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(CianAcento, CircleShape)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombreEjercicio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (primerBloque != null) {
                Text(
                    text = resumenBloque(bloques = bloques, bloque = primerBloque),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * @brief Compone el resumen legible de los bloques de un ejercicio.
 * @param bloques Bloques de series del ejercicio.
 * @param bloque Primer bloque de referencia para repeticiones y descanso.
 * @return Cadena con series × repeticiones, peso y descanso.
 */
private fun resumenBloque(bloques: List<BloqueRutina>, bloque: BloqueRutina): String {
    val series = bloques.size
    val textoSeries = "$series series × ${bloque.repeticiones} reps"
    val textoPeso = bloque.pesoKg?.let { " · ${it} kg" } ?: ""
    val textoDescanso = " · ${bloque.descansoSegundos}s descanso"
    return textoSeries + textoPeso + textoDescanso
}

/**
 * @brief Diálogo de construcción automática de una rutina PPL por día.
 * @param onConfirmar Acción de confirmación con el día seleccionado.
 * @param onCancelar Acción de cancelación del diálogo.
 */
@Composable
private fun DialogoConstruirRutina(
    onConfirmar: (diaSemana: Int) -> Unit,
    onCancelar: () -> Unit
) {
    var diaSeleccionado by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Construir rutina PPL") },
        text = {
            Column {
                Text(
                    text = "Elige el día de la semana para el que quieres construir " +
                        "la rutina automáticamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                DIAS_CONSTRUCCION.forEach { (dia, nombre) ->
                    FilterChip(
                        selected = diaSeleccionado == dia,
                        onClick = { diaSeleccionado = dia },
                        label = { Text(nombre) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(diaSeleccionado) },
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                Text("Construir", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = AzulSecundario) }
        }
    )
}