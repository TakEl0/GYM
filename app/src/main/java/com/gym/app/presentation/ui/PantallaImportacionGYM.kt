/**
 * @file PantallaImportacionGYM.kt
 * @brief Pantalla de importación de documentos Naturvitia en Jetpack Compose.
 * Permite seleccionar los tres documentos PDF (Informe InBody, Dieta y Plan de
 * entrenamiento) mediante el selector de archivos del sistema (ActivityResultContracts
 * OpenDocument), previsualizar las selecciones y ejecutar la importación, que
 * procesa los PDF con PDFBox y persiste los datos en los repositorios.
 */
package com.gym.app.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.usecase.importacion.ResultadoImportacionRutina
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.ImportacionViewModel
import com.gym.app.presentation.viewmodel.TipoDocumentoNaturvitia

/**
 * @brief Pantalla de importación de documentos Naturvitia.
 *
 * Ofrece una tarjeta por cada [TipoDocumentoNaturvitia] con su botón de selección
 * de PDF. Cuando un documento está seleccionado se muestra su nombre de archivo
 * y la opción de eliminarlo. El botón inferior "Importar documentos" ejecuta la
 * importación y muestra el resultado mediante Snackbar.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 * @param alVolver Acción de navegación de retroceso (opcional).
 */
@Composable
fun PantallaImportacionGYM(
    contenedor: ContenedorDependencias,
    alVolver: () -> Unit = {}
) {
    val viewModel: ImportacionViewModel = viewModel { ImportacionViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    // Tipo de documento al que se aplicará la próxima selección de PDF.
    var tipoSeleccionPendiente by remember { mutableStateOf<TipoDocumentoNaturvitia?>(null) }

    // Selector de PDF genérico que devuelve un único URI (tipo de documento aplicable).
    val selectorPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { uriSeleccionado ->
            tipoSeleccionPendiente?.let { tipo ->
                viewModel.seleccionarDocumento(tipo, uriSeleccionado)
            }
        }
    }

    LaunchedEffect(estado.mensajeExito, estado.error) {
        estado.mensajeExito?.let {
            snackbarHost.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
        estado.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = alVolver) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column {
                    Text(
                        text = "Importar documentos",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Carga los PDF de Naturvitia (dieta, entrenamiento e InBody)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            TipoDocumentoNaturvitia.entries.forEach { tipo ->
                TarjetaSeleccionDocumento(
                    tipo = tipo,
                    uriSeleccionada = estado.uris[tipo],
                    onSeleccionar = {
                        tipoSeleccionPendiente = tipo
                        selectorPdf.launch(arrayOf("application/pdf"))
                    },
                    onEliminar = { viewModel.eliminarDocumento(tipo) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            BotonImportar(
                habilitado = estado.uris.isNotEmpty() && !estado.importando,
                importando = estado.importando,
                onImportar = { viewModel.importar(contexto) }
            )

            // Resultado del mapeo de la rutina de entrenamiento a la maquinaria real.
            estado.resultadoRutina?.let { resultado ->
                Spacer(modifier = Modifier.height(16.dp))
                TarjetaResultadoRutina(resultado = resultado)
            }

            // Aviso no bloqueante si la rutina no se pudo importar (p. ej. gimnasio sin configurar).
            estado.avisoRutina?.let { aviso ->
                Spacer(modifier = Modifier.height(12.dp))
                TarjetaAvisoRutina(aviso = aviso)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Tarjeta de selección de un documento PDF concreto.
 * @param tipo Tipo de documento representado.
 * @param uriSeleccionada URI del archivo elegido (null si no hay selección).
 * @param onSeleccionar Acción de abrir el selector de PDF.
 * @param onEliminar Acción de eliminar la selección actual.
 */
@Composable
private fun TarjetaSeleccionDocumento(
    tipo: TipoDocumentoNaturvitia,
    uriSeleccionada: android.net.Uri?,
    onSeleccionar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SuperficieElevada, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = if (uriSeleccionada != null) CianAcento else AzulSecundario,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tipo.etiqueta,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (uriSeleccionada != null) {
                            uriSeleccionada.lastPathSegment ?: "PDF seleccionado"
                        } else {
                            "Ningún PDF seleccionado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uriSeleccionada != null) CianAcento
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (uriSeleccionada != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    OutlinedButton(onClick = onEliminar) {
                        Text("Quitar", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = onSeleccionar,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Cambiar archivo", color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = onSeleccionar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                ) {
                    Text("Seleccionar PDF", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * @brief Botón principal de importación con indicador de progreso.
 * @param habilitado Indica si el botón puede pulsarse (hay documentos y no está importando).
 * @param importando Indica si la importación está en curso.
 * @param onImportar Acción de importación.
 */
@Composable
private fun BotonImportar(
    habilitado: Boolean,
    importando: Boolean,
    onImportar: () -> Unit
) {
    Button(
        onClick = onImportar,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (importando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Procesando documentos…",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Importar documentos",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    if (!habilitado && !importando) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "Selecciona al menos un documento para importar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * @brief Tarjeta de resultado del mapeo de la rutina importada.
 *
 * Resume el resultado de [ResultadoImportacionRutina]: número de rutinas creadas y
 * ejercicios mapeados a las máquinas reales del gimnasio. Si quedaron ejercicios sin
 * mapear se muestra una sección de aviso con la lista de nombres pendientes.
 *
 * @param resultado Resumen de la importación de la rutina.
 */
@Composable
private fun TarjetaResultadoRutina(resultado: ResultadoImportacionRutina) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SuperficieElevada, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = CianAcento,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rutina importada en tu gimnasio",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Se crearon ${resultado.rutinasCreadas.size} rutinas con " +
                            "${resultado.ejerciciosMapeados} ejercicios mapeados a las máquinas " +
                            "de Fitness Park (${resultado.rutinasCreadas.size} días).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (resultado.ejerciciosSinMapear.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SeccionEjerciciosSinMapear(ejercicios = resultado.ejerciciosSinMapear)
            }
        }
    }
}

/**
 * @brief Sección de aviso con los ejercicios del plan que no se resolvieron contra
 * ninguna máquina del gimnasio.
 *
 * Muestra hasta [MAX_EJERCICIOS_SIN_MAPEAR_VISIBLES] nombres y un contador "+N más"
 * si hay más. La confirmación manual o asistida por IA llegará en una fase posterior;
 * por ahora solo se informa al usuario para que pueda revisarlos.
 *
 * @param ejercicios Nombres de los ejercicios pendientes de mapear.
 */
@Composable
private fun SeccionEjerciciosSinMapear(ejercicios: List<String>) {
    val visibles = ejercicios.take(MAX_EJERCICIOS_SIN_MAPEAR_VISIBLES)
    val ocultos = ejercicios.size - visibles.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SuperficieElevada, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "${ejercicios.size} ejercicios sin mapear:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        visibles.forEach { nombre ->
            Text(
                text = "• $nombre",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (ocultos > 0) {
            Text(
                text = "+$ocultos más",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Podrás revisarlos y confirmarlos manualmente en una fase posterior.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * @brief Tarjeta informativa no bloqueante cuando la rutina no se pudo importar
 * (p. ej. gimnasio sin maquinaria configurada). El resto de documentos ya
 * importados conservan su éxito.
 * @param aviso Mensaje de aviso con el motivo del fallo.
 */
@Composable
private fun TarjetaAvisoRutina(aviso: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "Rutina no importada",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = aviso,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Número máximo de ejercicios sin mapear mostrados en la tarjeta de resultado. */
private const val MAX_EJERCICIOS_SIN_MAPEAR_VISIBLES: Int = 5