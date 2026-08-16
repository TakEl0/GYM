/**
 * @file PantallaRegistroPesoGYM.kt
 * @brief Pantalla de registro de peso corporal de la aplicación GYM.
 * Permite al usuario introducir su peso (y opcionalmente el porcentaje de
 * grasa corporal), guardarlo y consultar el historial de mediciones.
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.domain.model.RegistroPeso
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.BlancoAzulado
import com.gym.app.presentation.ui.theme.CianAcento
import com.gym.app.presentation.ui.theme.GrisAzulado
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.RegistroPesoViewModel

/**
 * @brief Pantalla de registro de peso de GYM.
 * Crea el ViewModel y muestra el formulario de alta junto con el historial
 * de mediciones del usuario, delegando la lógica en la capa de dominio.
 */
@Composable
fun PantallaRegistroPesoGYM() {
    val viewModel: RegistroPesoViewModel = viewModel()
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial()
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
            Text(
                text = "Registro de Peso",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Monitoriza tu recomposición corporal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            TarjetaPesoActual(estado.ultimoRegistro)
            Spacer(modifier = Modifier.height(20.dp))
            TarjetaNuevoRegistro(
                pesoActual = estado.pesoActual,
                grasaActual = estado.grasaActual,
                onPesoChanged = viewModel::actualizarPeso,
                onGrasaChanged = viewModel::actualizarGrasa,
                onGuardar = viewModel::guardarRegistro
            )
            estado.mensajeGuardado?.let { mensaje ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CianAcento
                )
            }
            estado.error?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Historial",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            ListaHistorial(
                historial = estado.historial,
                formatearFecha = viewModel::formatearFecha
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Tarjeta con el último peso registrado.
 * @param ultimoRegistro Último registro de peso (puede ser null).
 */
@Composable
private fun TarjetaPesoActual(ultimoRegistro: RegistroPeso?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(AzulPrimario, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MonitorWeight,
                    contentDescription = "Peso actual",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(
                    text = "Peso actual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (ultimoRegistro != null) {
                        "${ultimoRegistro.pesoKg} kg"
                    } else {
                        "Sin registros"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * @brief Tarjeta con el formulario para registrar un nuevo peso.
 * @param pesoActual Texto actual del campo de peso.
 * @param grasaActual Texto actual del campo de grasa corporal.
 * @param onPesoChanged Callback al editar el campo de peso.
 * @param onGrasaChanged Callback al editar el campo de grasa.
 * @param onGuardar Callback al pulsar el botón de guardar.
 */
@Composable
private fun TarjetaNuevoRegistro(
    pesoActual: String,
    grasaActual: String,
    onPesoChanged: (String) -> Unit,
    onGrasaChanged: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nuevo registro",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pesoActual,
                onValueChange = onPesoChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Peso (kg)") },
                placeholder = { Text("Ej. 81,5") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampoTexto()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = grasaActual,
                onValueChange = onGrasaChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Grasa corporal (%) — opcional") },
                placeholder = { Text("Ej. 17,8") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampoTexto()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Guardar registro",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * @brief Devuelve los colores personalizados de los campos de texto.
 * Ajusta el tema oscuro azulado a los campos de entrada del formulario.
 * @return Objeto TextFieldColors con los colores del tema.
 */
@Composable
private fun coloresCampoTexto(): androidx.compose.material3.TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = BlancoAzulado,
        unfocusedTextColor = BlancoAzulado,
        focusedBorderColor = AzulPrimario,
        unfocusedBorderColor = SuperficieElevada,
        focusedLabelColor = AzulSecundario,
        unfocusedLabelColor = GrisAzulado,
        cursorColor = AzulPrimario
    )

/**
 * @brief Lista del historial de registros de peso.
 * @param historial Registros ordenados del más reciente al más antiguo.
 * @param formatearFecha Función que formatea una fecha para la UI.
 */
@Composable
private fun ListaHistorial(
    historial: List<RegistroPeso>,
    formatearFecha: (java.time.LocalDate) -> String
) {
    if (historial.isEmpty()) {
        Text(
            text = "Aún no hay registros de peso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        historial.forEach { registro ->
            FilaHistorial(registro = registro, formatearFecha = formatearFecha)
        }
    }
}

/**
 * @brief Fila individual del historial de peso.
 * @param registro Registro de peso a mostrar.
 * @param formatearFecha Función que formatea la fecha del registro.
 */
@Composable
private fun FilaHistorial(
    registro: RegistroPeso,
    formatearFecha: (java.time.LocalDate) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${registro.pesoKg} kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatearFecha(registro.fecha),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            registro.grasaCorporalPorcentaje?.let { grasa ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = "Grasa corporal",
                        tint = AzulSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "$grasa% grasa",
                        style = MaterialTheme.typography.labelMedium,
                        color = AzulSecundario
                    )
                }
            }
        }
    }
}