package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.ComunidadViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaComunidadGYM(contenedor: ContenedorDependencias) {
    val context = LocalContext.current
    val viewModel: ComunidadViewModel = viewModel {
        ComunidadViewModel(context, contenedor)
    }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    var textoPublicacion by remember { mutableStateOf("") }
    var urlImagen by remember { mutableStateOf("") }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = AzulPrimario,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva publicación")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Comunidad GYM",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Comparte tus entrenamientos, motiva a otros y consulta el calendario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Próximos Eventos Grupales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (estado.eventos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
                ) {
                    Text(
                        text = "No hay eventos próximos programados.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                estado.eventos.forEach { evento ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = CianAcento,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = evento.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                evento.descripcion?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Muro de Actividad",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (estado.cargando) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
            } else if (estado.publicaciones.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
                ) {
                    Text(
                        text = "Aún no hay publicaciones en la comunidad. ¡Sé el primero en compartir tu entrenamiento!",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                estado.publicaciones.forEach { publicacion ->
                    TarjetaPublicacion(
                        publicacion = publicacion,
                        onReaccionar = { tipo ->
                            viewModel.reaccionar(publicacion.id, tipo)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCrear = false },
            title = { Text("Compartir Entrenamiento") },
            text = {
                Column {
                    OutlinedTextField(
                        value = textoPublicacion,
                        onValueChange = { textoPublicacion = it },
                        label = { Text("¿Cómo ha ido tu sesión de hoy?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = urlImagen,
                        onValueChange = { urlImagen = it },
                        label = { Text("URL de imagen (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (textoPublicacion.isNotBlank()) {
                        viewModel.crearPublicacion(
                            contenido = textoPublicacion,
                            urlImagen = urlImagen.ifBlank { null }
                        )
                        textoPublicacion = ""
                        urlImagen = ""
                        mostrarDialogoCrear = false
                    }
                }) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrear = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaPublicacion(
    publicacion: com.gym.app.domain.model.Publicacion,
    onReaccionar: (String) -> Unit
) {
    val formatoFecha = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
    val fechaStr = Instant.ofEpochMilli(publicacion.fecha)
        .atZone(ZoneId.systemDefault())
        .format(formatoFecha)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AzulPrimario, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = publicacion.autorNombre.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = publicacion.autorNombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = fechaStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = publicacion.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { onReaccionar("LIKE") },
                    label = { Text("❤️ ${publicacion.reacciones.count { it.tipoReaccion == "LIKE" }}") }
                )
                AssistChip(
                    onClick = { onReaccionar("FIRE") },
                    label = { Text("🔥 ${publicacion.reacciones.count { it.tipoReaccion == "FIRE" }}") }
                )
                AssistChip(
                    onClick = { onReaccionar("MUSCLE") },
                    label = { Text("💪 ${publicacion.reacciones.count { it.tipoReaccion == "MUSCLE" }}") }
                )
            }
        }
    }
}
