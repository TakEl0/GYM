/**
 * @file PantallaGimnasioGYM.kt
 * @brief Pantalla de Gimnasio de la aplicación GYM en Jetpack Compose.
 * Muestra el gimnasio configurado con su parque de máquinas, permite guardar el
 * gimnasio, registrar nuevas máquinas mediante un diálogo y consultar ejercicios
 * alternativos cuando una máquina no está disponible.
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.GimnasioViewModel

/**
 * @brief Pantalla de Gimnasio de la aplicación GYM.
 *
 * Crea el [GimnasioViewModel] a partir del [ContenedorDependencias]. Si el
 * gimnasio aún no está configurado se muestra un formulario de guardado con su
 * nombre; una vez configurado, se listan sus máquinas con el grupo muscular y el
 * estado de disponibilidad. El botón flotante abre el diálogo de alta de máquina
 * y cada máquina ofrece la consulta de ejercicios alternativos.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun PantallaGimnasioGYM(contenedor: ContenedorDependencias) {
    val viewModel: GimnasioViewModel = viewModel { GimnasioViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    var mostrarDialogoMaquina by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoMaquina = true },
                containerColor = AzulPrimario,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir máquina")
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
                CabeceraGimnasio()
                Spacer(modifier = Modifier.height(16.dp))
            }

            val gimnasio = estado.gimnasio
            if (gimnasio == null) {
                if (!estado.cargando) {
                    FormularioGuardarGimnasio(onGuardar = viewModel::guardarGimnasio)
                }
            } else {
                TarjetaInfoGimnasio(gimnasio = gimnasio)
                Spacer(modifier = Modifier.height(8.dp))
                ListaMaquinas(
                    gimnasio = gimnasio,
                    onConsultarAlternativas = viewModel::consultarAlternativas
                )
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

    if (mostrarDialogoMaquina) {
        DialogoRegistrarMaquina(
            onConfirmar = { nombre, grupos ->
                viewModel.registrarMaquina(nombre, grupos)
                mostrarDialogoMaquina = false
            },
            onCancelar = { mostrarDialogoMaquina = false }
        )
    }

    if (estado.alternativas.isNotEmpty() || estado.consultandoAlternativas) {
        DialogoAlternativas(
            alternativas = estado.alternativas,
            consultando = estado.consultandoAlternativas,
            onCerrar = viewModel::cerrarAlternativas
        )
    }
}

/**
 * @brief Cabecera de la pantalla de Gimnasio.
 */
@Composable
private fun CabeceraGimnasio() {
    Column {
        Text(
            text = "Gimnasio",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Configura tu centro y su maquinaria",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * @brief Formulario de guardado del gimnasio cuando aún no está configurado.
 * @param onGuardar Acción de guardado con el nombre y la dirección introducidos.
 */
@Composable
private fun FormularioGuardarGimnasio(onGuardar: (String, String?) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Configura tu gimnasio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Indica el nombre de tu centro para comenzar a gestionar " +
                    "su maquinaria.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del gimnasio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onGuardar(nombre, direccion.ifBlank { null }) },
                enabled = nombre.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Guardar gimnasio",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * @brief Tarjeta con la información básica del gimnasio configurado.
 * @param gimnasio Gimnasio configurado.
 */
@Composable
private fun TarjetaInfoGimnasio(gimnasio: Gimnasio) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = gimnasio.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            gimnasio.direccion?.takeIf { it.isNotBlank() }?.let { direccion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AzulSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${gimnasio.maquinas.size} máquinas registradas",
                style = MaterialTheme.typography.labelMedium,
                color = AzulSecundario
            )
        }
    }
}

/**
 * @brief Lista de máquinas del gimnasio con su grupo muscular y disponibilidad.
 * @param gimnasio Gimnasio que contiene las máquinas.
 * @param onConsultarAlternativas Acción al pulsar "Alternativas" en una máquina.
 */
@Composable
private fun ListaMaquinas(
    gimnasio: Gimnasio,
    onConsultarAlternativas: (String) -> Unit
) {
    if (gimnasio.maquinas.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
        ) {
            Text(
                text = "Aún no has registrado máquinas. Pulsa el botón + para añadir la primera.",
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(gimnasio.maquinas, key = { it.id }) { maquina ->
            FilaMaquina(
                maquina = maquina,
                onConsultarAlternativas = onConsultarAlternativas
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * @brief Fila con una máquina del gimnasio y su estado de disponibilidad.
 * @param maquina Máquina a representar.
 * @param onConsultarAlternativas Acción para consultar ejercicios alternativos.
 */
@Composable
private fun FilaMaquina(
    maquina: Maquina,
    onConsultarAlternativas: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SuperficieElevada, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = if (maquina.disponible) CianAcento else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = maquina.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = maquina.grupoMuscular.joinToString(" · ")
                            .ifEmpty { "Grupo muscular sin especificar" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IndicadorDisponibilidad(disponible = maquina.disponible)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { onConsultarAlternativas(maquina.id) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Filled.Yard,
                    contentDescription = null,
                    tint = AzulSecundario,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Ver alternativas", color = AzulSecundario)
            }
        }
    }
}

/**
 * @brief Indicador visual de disponibilidad de una máquina.
 * @param disponible Indica si la máquina está operativa y libre.
 */
@Composable
private fun IndicadorDisponibilidad(disponible: Boolean) {
    val color = if (disponible) CianAcento else MaterialTheme.colorScheme.error
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (disponible) "Disponible" else "No disponible",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * @brief Diálogo de registro de una nueva máquina del gimnasio.
 * @param onConfirmar Acción de confirmación con nombre y grupos musculares.
 * @param onCancelar Acción de cancelación del diálogo.
 */
@Composable
private fun DialogoRegistrarMaquina(
    onConfirmar: (nombre: String, gruposMusculares: List<String>) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var grupos by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Añadir máquina") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la máquina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = grupos,
                    onValueChange = { grupos = it },
                    label = { Text("Grupo muscular (separado por comas)") },
                    placeholder = { Text("Ej. CUADRICEPS, GLUTEO") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmar(
                        nombre,
                        grupos.split(',').map { it.trim() }.filter { it.isNotEmpty() }
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
 * @brief Diálogo con los ejercicios alternativos sugeridos para una máquina.
 * @param alternativas Ejercicios alternativos encontrados.
 * @param consultando Indica si la consulta está en curso.
 * @param onCerrar Acción de cierre del diálogo.
 */
@Composable
private fun DialogoAlternativas(
    alternativas: List<com.gym.app.domain.model.Ejercicio>,
    consultando: Boolean,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Ejercicios alternativos") },
        text = {
            if (consultando) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AzulPrimario,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text("Consultando alternativas...")
                }
            } else if (alternativas.isEmpty()) {
                Text(
                    "No se encontraron ejercicios alternativos para esta máquina."
                )
            } else {
                Column {
                    alternativas.forEach { ejercicio ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FitnessCenter,
                                contentDescription = null,
                                tint = CianAcento,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Column {
                                Text(
                                    text = ejercicio.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = ejercicio.grupoMuscularPrincipal,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar", color = AzulSecundario) }
        }
    )
}