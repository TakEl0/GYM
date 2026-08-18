/**
 * @file PantallaEntrenamientoEnVivoGYM.kt
 * @brief Pantalla de entrenamiento en vivo de la aplicación GYM en Jetpack Compose.
 * Muestra la sesión activa: cabecera con el nombre de la rutina y el cronómetro total,
 * barra de progreso de series, lista de ejercicios con sus máquinas reales y chips de
 * series registradas, barra inferior de descanso con avisos (sonido y vibración) y el
 * diálogo de registro/edición de series. Al finalizar la sesión, el resumen estadístico
 * se muestra dentro de la misma pantalla (sin navegación adicional).
 */
package com.gym.app.presentation.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.EjercicioConMaquina
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.SesionActivaViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** Estado del diálogo de serie: registro de una nueva serie o edición de una existente. */
private sealed interface DialogoSerie {

    /** Registro de una serie para el ejercicio indicado. */
    data class Registrar(val ejercicioId: String) : DialogoSerie

    /** Edición de una serie ya registrada. */
    data class Editar(val serie: SerieRealizada) : DialogoSerie
}

/**
 * @brief Pantalla de entrenamiento en vivo.
 *
 * Crea el [SesionActivaViewModel] a partir del [ContenedorDependencias] e inicia la
 * sesión de la rutina indicada. Muestra la cabecera (nombre, cronómetro total y barra
 * de progreso), la lista de ejercicios con sus series registradas, la barra inferior
 * de descanso y los diálogos de registro/edición de series. Cuando la sesión finaliza,
 * muestra el resumen estadístico dentro de la propia pantalla reutilizando
 * [PantallaResumenSesionGYM].
 *
 * @param rutinaId Identificador de la rutina a ejecutar.
 * @param contenedor Contenedor de dependencias de la aplicación.
 * @param alVolver Acción de navegación de retroceso.
 * @param alFinalizar Acción tras finalizar la sesión con su identificador (opcional;
 * el resumen se muestra dentro de la propia pantalla, por lo que por defecto no navega).
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun PantallaEntrenamientoEnVivoGYM(
    rutinaId: String,
    contenedor: ContenedorDependencias,
    alVolver: () -> Unit,
    alFinalizar: (sesionId: String) -> Unit = {}
) {
    val viewModel: SesionActivaViewModel = viewModel { SesionActivaViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    var dialogoSerie by remember { mutableStateOf<DialogoSerie?>(null) }
    var mostrarConfirmacionFinalizar by remember { mutableStateOf(false) }

    // Inicia la sesión en vivo al entrar en la pantalla.
    LaunchedEffect(rutinaId) { viewModel.iniciarSesion(rutinaId) }

    // Muestra los errores del ViewModel mediante Snackbar.
    LaunchedEffect(estado.error) {
        estado.error?.let { mensaje ->
            snackbarHost.showSnackbar(mensaje)
            viewModel.limpiarError()
        }
    }

    // Cuando la sesión finaliza se muestra el resumen dentro de la misma pantalla,
    // evitando pasar objetos complejos por argumentos de navegación.
    if (estado.finalizada) {
        estado.resumen?.let { resumen ->
            val duracionMinutos = if (estado.segundosTranscurridos > 0) {
                (estado.segundosTranscurridos / SEGUNDOS_POR_MINUTO).coerceAtLeast(1L).toInt()
            } else {
                0
            }
            PantallaResumenSesionGYM(
                resumen = resumen,
                duracionMinutos = duracionMinutos,
                alVolver = alVolver
            )
        }
        return
    }

    // La barra de descanso se muestra mientras hay tiempo de descanso (corriendo o
    // pausado) o cuando el descanso terminó y queda el aviso pendiente.
    val mostrarBarraDescanso = estado.descansoRestante > 0 || estado.descansoTerminado

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            if (mostrarBarraDescanso) {
                BarraDescanso(
                    descansoRestante = estado.descansoRestante,
                    descansoTotal = estado.descansoTotal,
                    descansoActivo = estado.descansoActivo,
                    descansoTerminado = estado.descansoTerminado,
                    nombreEjercicio = estado.ejercicios
                        .firstOrNull { it.bloque.ejercicioId == estado.ejercicioActualId }
                        ?.ejercicio?.nombre,
                    onSaltar = viewModel::saltarDescanso,
                    onAjustar = viewModel::ajustarDescanso,
                    onPausarReanudar = viewModel::pausarReanudarDescanso
                )
            }
        }
    ) { innerPadding ->
        if (estado.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AzulPrimario)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                CabeceraSesionEnVivo(
                    nombreRutina = estado.rutina?.nombre.orEmpty(),
                    segundos = estado.segundosTranscurridos,
                    seriesCompletadas = estado.seriesCompletadas,
                    seriesTotales = estado.seriesTotales,
                    alVolver = alVolver,
                    onFinalizar = {
                        val sinCompletar = estado.totalEjercicios - estado.ejerciciosCompletados
                        if (sinCompletar > 0) {
                            mostrarConfirmacionFinalizar = true
                        } else {
                            viewModel.finalizarSesion()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estado.ejercicios, key = { it.bloque.ejercicioId }) { ejercicio ->
                        val seriesDelEjercicio = estado.series.filter {
                            it.ejercicioId == ejercicio.bloque.ejercicioId
                        }
                        TarjetaEjercicioEnVivo(
                            ejercicio = ejercicio,
                            seriesDelEjercicio = seriesDelEjercicio,
                            onRegistrar = {
                                dialogoSerie = DialogoSerie.Registrar(ejercicio.bloque.ejercicioId)
                            },
                            onEditarSerie = { serie -> dialogoSerie = DialogoSerie.Editar(serie) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de registro o edición de una serie.
    when (val dialogo = dialogoSerie) {
        is DialogoSerie.Registrar -> {
            val ejercicio = estado.ejercicios
                .firstOrNull { it.bloque.ejercicioId == dialogo.ejercicioId }
            val cargaSugerida = viewModel.cargaSugeridaDe(dialogo.ejercicioId)
            DialogoSerieEntrenamiento(
                titulo = "Registrar serie",
                pesoInicial = cargaSugerida?.let(::formatearKg) ?: "",
                repeticionesInicial = ejercicio?.bloque?.repeticiones?.toString() ?: "",
                textoAyuda = cargaSugerida?.let { "Sugerido: ${formatearKg(it)} kg" },
                onGuardar = { peso, repeticiones ->
                    viewModel.registrarSerie(dialogo.ejercicioId, peso, repeticiones)
                    dialogoSerie = null
                },
                onCancelar = { dialogoSerie = null }
            )
        }

        is DialogoSerie.Editar -> {
            DialogoSerieEntrenamiento(
                titulo = "Editar serie",
                pesoInicial = formatearKg(dialogo.serie.pesoKg),
                repeticionesInicial = dialogo.serie.repeticiones.toString(),
                textoAyuda = null,
                onGuardar = { peso, repeticiones ->
                    viewModel.editarSerie(dialogo.serie.id, peso, repeticiones)
                    dialogoSerie = null
                },
                onCancelar = { dialogoSerie = null }
            )
        }

        null -> Unit
    }

    // Confirmación al finalizar la sesión con ejercicios sin completar.
    if (mostrarConfirmacionFinalizar) {
        val sinCompletar = estado.totalEjercicios - estado.ejerciciosCompletados
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionFinalizar = false },
            title = { Text("Finalizar sesión") },
            text = {
                Text(
                    text = "Te quedan $sinCompletar ejercicios sin completar. " +
                        "¿Finalizar de todos modos?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionFinalizar = false
                        viewModel.finalizarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                ) {
                    Text("Finalizar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionFinalizar = false }) {
                    Text("Seguir entrenando", color = AzulSecundario)
                }
            }
        )
    }
}

/**
 * @brief Cabecera de la sesión en vivo con el nombre de la rutina, el cronómetro
 * total en `mm:ss`, la barra de progreso de series y el botón de finalizar.
 * @param nombreRutina Nombre de la rutina ejecutada.
 * @param segundos Segundos transcurridos de la sesión.
 * @param seriesCompletadas Series registradas hasta el momento.
 * @param seriesTotales Series totales planificadas.
 * @param alVolver Acción de navegación de retroceso.
 * @param onFinalizar Acción al pulsar el botón de finalizar la sesión.
 */
@Composable
private fun CabeceraSesionEnVivo(
    nombreRutina: String,
    segundos: Long,
    seriesCompletadas: Int,
    seriesTotales: Int,
    alVolver: () -> Unit,
    onFinalizar: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreRutina.ifBlank { "Sesión de entrenamiento" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatearCronometro(segundos),
                    style = MaterialTheme.typography.titleLarge,
                    color = CianAcento,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Button(
                onClick = onFinalizar,
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Finalizar", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        val progreso = if (seriesTotales > 0) {
            seriesCompletadas.toFloat() / seriesTotales
        } else {
            0f
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = CianAcento,
            trackColor = SuperficieElevada
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Series $seriesCompletadas/$seriesTotales",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * @brief Tarjeta de un ejercicio de la sesión en vivo con su máquina real, los chips
 * de series registradas (pulsables para editar) y el botón de registrar una serie.
 * @param ejercicio Ejercicio con su bloque prescrito y la máquina resuelta.
 * @param seriesDelEjercicio Series registradas de este ejercicio en la sesión.
 * @param onRegistrar Acción al pulsar "Registrar serie".
 * @param onEditarSerie Acción al pulsar un chip de serie (edición).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaEjercicioEnVivo(
    ejercicio: EjercicioConMaquina,
    seriesDelEjercicio: List<SerieRealizada>,
    onRegistrar: () -> Unit,
    onEditarSerie: (SerieRealizada) -> Unit
) {
    val seriesPrescritas = ejercicio.bloque.serie
    val completado = seriesDelEjercicio.size >= seriesPrescritas

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ejercicio.ejercicio.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = descripcionMaquina(ejercicio.maquina),
                        style = MaterialTheme.typography.bodySmall,
                        color = AzulSecundario
                    )
                }
                if (completado) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Ejercicio completado",
                        tint = CianAcento,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (seriesDelEjercicio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    seriesDelEjercicio.forEach { serie ->
                        SuggestionChip(
                            onClick = { onEditarSerie(serie) },
                            label = {
                                Text(
                                    text = "${serie.numeroSerie} · " +
                                        "${formatearKg(serie.pesoKg)} kg × ${serie.repeticiones}"
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${seriesDelEjercicio.size}/$seriesPrescritas series",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onRegistrar,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Registrar serie", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * @brief Barra inferior fija de descanso con el círculo de cuenta atrás, el nombre del
 * ejercicio actual y los controles Saltar / +15s / −15s / Pausar-Reanudar. Cuando el
 * descanso llega a cero reproduce la vibración y el tono de aviso y muestra el mensaje
 * "¡Siguiente serie!".
 * @param descansoRestante Segundos restantes del descanso.
 * @param descansoTotal Duración configurada del descanso actual.
 * @param descansoActivo Indica si el cronómetro de descanso está corriendo.
 * @param descansoTerminado Indica si el descanso llegó a cero (aviso pendiente).
 * @param nombreEjercicio Nombre del ejercicio sobre el que se descansa.
 * @param onSaltar Acción de saltar el descanso.
 * @param onAjustar Acción de ajustar el descanso en el delta indicado (segundos).
 * @param onPausarReanudar Acción de pausar o reanudar el cronómetro.
 */
@Composable
private fun BarraDescanso(
    descansoRestante: Int,
    descansoTotal: Int,
    descansoActivo: Boolean,
    descansoTerminado: Boolean,
    nombreEjercicio: String?,
    onSaltar: () -> Unit,
    onAjustar: (Int) -> Unit,
    onPausarReanudar: () -> Unit
) {
    val contexto = LocalContext.current

    // Aviso sonoro + vibración cuando el descanso llega a cero.
    LaunchedEffect(descansoTerminado) {
        if (descansoTerminado) {
            reproducirAviso(contexto)
        }
    }

    Surface(
        color = SuperficieOscura,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (descansoTerminado) {
                Text(
                    text = "¡Siguiente serie!",
                    style = MaterialTheme.typography.titleMedium,
                    color = CianAcento,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = {
                            if (descansoTotal > 0) {
                                descansoRestante.toFloat() / descansoTotal
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        color = if (descansoTerminado) CianAcento else AzulPrimario,
                        trackColor = SuperficieElevada,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = formatearMinutosSegundos(descansoRestante.toLong()),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = "Descanso",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = nombreEjercicio ?: "Siguiente ejercicio",
                        style = MaterialTheme.typography.bodySmall,
                        color = AzulSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onSaltar) {
                    Text("Saltar", color = AzulSecundario)
                }
                TextButton(onClick = { onAjustar(-SEGUNDOS_AJUSTE) }) {
                    Text("−15 s", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onAjustar(SEGUNDOS_AJUSTE) }) {
                    Text("+15 s", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onPausarReanudar) {
                    Text(
                        text = if (descansoActivo) "Pausar" else "Reanudar",
                        color = AzulSecundario
                    )
                }
            }
        }
    }
}

/**
 * @brief Diálogo reutilizable de registro o edición de una serie.
 * Muestra campos numéricos de peso (kg) y repeticiones, con un texto auxiliar opcional
 * de carga sugerida, y los botones Guardar / Cancelar.
 * @param titulo Título del diálogo ("Registrar serie" o "Editar serie").
 * @param pesoInicial Valor inicial del campo de peso.
 * @param repeticionesInicial Valor inicial del campo de repeticiones.
 * @param textoAyuda Texto auxiliar opcional (carga sugerida).
 * @param onGuardar Acción de confirmación con el peso y las repeticiones.
 * @param onCancelar Acción de cancelación del diálogo.
 */
@Composable
private fun DialogoSerieEntrenamiento(
    titulo: String,
    pesoInicial: String,
    repeticionesInicial: String,
    textoAyuda: String?,
    onGuardar: (pesoKg: Double, repeticiones: Int) -> Unit,
    onCancelar: () -> Unit
) {
    var peso by remember { mutableStateOf(pesoInicial) }
    var repeticiones by remember { mutableStateOf(repeticionesInicial) }

    val pesoValido = peso.toDoubleOrNull()?.let { it > 0.0 } == true
    val repeticionesValidas = repeticiones.toIntOrNull()?.let { it >= 1 } == true

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it.filter { caracter -> caracter.isDigit() || caracter == '.' } },
                    label = { Text("Peso (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = textoAyuda?.let { ayuda -> { Text(ayuda) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repeticiones,
                    onValueChange = { repeticiones = it.filter { caracter -> caracter.isDigit() } },
                    label = { Text("Repeticiones") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(peso.toDoubleOrNull() ?: 0.0, repeticiones.toIntOrNull() ?: 0)
                },
                enabled = pesoValido && repeticionesValidas,
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = AzulSecundario)
            }
        }
    )
}

/**
 * @brief Reproduce la vibración (300 ms) y el tono breve de aviso cuando el descanso
 * termina. Ambas operaciones se ejecutan en bloques try/catch para no romper la
 * sesión si el dispositivo no las soporta.
 * @param contexto Contexto de la aplicación para acceder al Vibrator.
 */
private suspend fun reproducirAviso(contexto: Context) {
    try {
        val vibrator = contexto.getSystemService(Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    DURACION_VIBRACION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(DURACION_VIBRACION_MS)
        }
    } catch (_: Exception) {
        // Si el dispositivo no ofrece vibración se ignora silenciosamente.
    }

    var tono: ToneGenerator? = null
    try {
        tono = ToneGenerator(AudioManager.STREAM_MUSIC, VOLUMEN_TONO)
        tono.startTone(ToneGenerator.TONE_PROP_BEEP2, DURACION_TONO_MS)
    } catch (_: Exception) {
        // Si el audio no está disponible se ignora silenciosamente.
    }
    delay(DURACION_TONO_MS + 100L)
    tono?.release()
}

/**
 * @brief Describe la máquina real sobre la que se ejecuta un ejercicio.
 * @param maquina Máquina resuelta (puede ser null para peso libre).
 * @return "Marca · Modelo", el nombre de la máquina o "Peso libre".
 */
private fun descripcionMaquina(maquina: Maquina?): String = when {
    maquina == null -> "Peso libre"
    !maquina.marca.isNullOrBlank() && !maquina.modelo.isNullOrBlank() ->
        "${maquina.marca} · ${maquina.modelo}"
    maquina.nombre.isNotBlank() -> maquina.nombre
    else -> "Peso libre"
}

/**
 * @brief Formatea los segundos como cronómetro `mm:ss` con dos dígitos.
 * @param segundos Total de segundos.
 * @return Cadena formateada (p. ej. "03:45").
 */
private fun formatearCronometro(segundos: Long): String {
    val minutos = segundos / SEGUNDOS_POR_MINUTO
    val resto = segundos % SEGUNDOS_POR_MINUTO
    return "%02d:%02d".format(minutos, resto)
}

/**
 * @brief Formatea los segundos como tiempo corto `m:ss` para el descanso.
 * @param segundos Total de segundos.
 * @return Cadena formateada (p. ej. "0:47").
 */
private fun formatearMinutosSegundos(segundos: Long): String {
    val minutos = segundos / SEGUNDOS_POR_MINUTO
    val resto = segundos % SEGUNDOS_POR_MINUTO
    return "%d:%02d".format(minutos, resto)
}

/**
 * @brief Formatea un peso en kilogramos: entero si no tiene decimales, con un
 * decimal en caso contrario (p. ej. "80" o "62.5").
 * @param valor Peso en kilogramos.
 * @return Cadena formateada del peso.
 */
private fun formatearKg(valor: Double): String {
    val redondeado = (valor * 10.0).roundToInt() / 10.0
    return if (redondeado % 1.0 == 0.0) {
        redondeado.roundToInt().toString()
    } else {
        redondeado.toString()
    }
}

/** Segundos que tiene un minuto (para el cronómetro y la duración). */
private const val SEGUNDOS_POR_MINUTO: Long = 60L

/** Ajuste rápido del descanso en segundos (+15 / −15). */
private const val SEGUNDOS_AJUSTE: Int = 15

/** Duración de la vibración de aviso en milisegundos. */
private const val DURACION_VIBRACION_MS: Long = 300L

/** Duración del tono de aviso en milisegundos. */
private const val DURACION_TONO_MS: Int = 150

/** Volumen del tono de aviso (1-100). */
private const val VOLUMEN_TONO: Int = 80