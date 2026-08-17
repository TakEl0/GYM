/**
 * @file PantallaPerfilGYM.kt
 * @brief Pantalla de perfil de usuario de la aplicación GYM en Jetpack Compose.
 * Muestra los datos reales del usuario (nombre y correo de la sesión activa),
 * permite EDITAR el nombre y los objetivos nutricionales (peso objetivo, altura,
 * edad, sexo, factor de actividad y objetivo) y ofrece el botón de "Cerrar sesión".
 * Toda la lógica de negocio reside en el [PerfilViewModel] y en los casos de uso
 * de la capa de dominio; no se muestran datos inventados.
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.CampoPerfil
import com.gym.app.presentation.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

/**
 * @brief Pantalla de perfil de usuario de GYM.
 * Observa el perfil real del usuario mediante el [PerfilViewModel], permite
 * editar el nombre y los objetivos nutricionales y cerrar la sesión.
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun PantallaPerfilGYM(contenedor: ContenedorDependencias) {
    val alcance = rememberCoroutineScope()
    var cerrandoSesion by remember { mutableStateOf(false) }
    val correoSesion = remember {
        contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.email
    }
    val usuarioId = remember {
        contenedor.obtenerSesionActualCasoUso.ejecutar()?.user?.id ?: ""
    }
    val snackbarHost = remember { SnackbarHostState() }

    // El ViewModel se construye solo si disponemos de un identificador de usuario;
    // de lo contrario se muestra la tarjeta con el correo y el cierre de sesión.
    if (usuarioId.isBlank()) {
        PerfilSinUsuario(
            correoSesion = correoSesion,
            cerrandoSesion = cerrandoSesion,
            onCerrarSesion = {
                cerrandoSesion = true
                alcance.launch { contenedor.cerrarSesionCasoUso.ejecutar() }
            }
        )
        return
    }

    val viewModel: PerfilViewModel = viewModel { PerfilViewModel(usuarioId, contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Notifica los mensajes temporales de éxito/error mediante Snackbar.
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
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tu cuenta, tus datos y tus objetivos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta con el nombre real (editable), alias (editable) y correo.
            TarjetaDatosUsuario(
                correoSesion = correoSesion,
                nombreUsuario = estado.perfil?.nombre,
                nombreEditado = estado.nombreEditado,
                onNombreCambiado = viewModel::actualizarNombre,
                aliasEditado = estado.aliasEditado,
                onAliasCambiado = viewModel::actualizarAlias
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para guardar el nombre y el alias editados.
            Button(
                onClick = viewModel::guardarPerfilCompleto,
                enabled = !estado.guardando && estado.nombreEditado.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar nombre y alias")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Datos antropométricos y objetivos editables.
            Text(
                text = "Datos y objetivos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estos datos se usan para calcular tu metabolismo (Mifflin-St Jeor) y tus objetivos diarios.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val perfil = estado.perfil
            if (perfil == null) {
                Text(
                    text = "Cargando tu perfil…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FormularioObjetivos(
                    perfil = perfil,
                    guardando = estado.guardando,
                    onCampoCambiado = viewModel::actualizarCampo,
                    onGuardar = viewModel::guardarObjetivos
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cierre de sesión.
            OutlinedButton(
                onClick = {
                    cerrandoSesion = true
                    alcance.launch { contenedor.cerrarSesionCasoUso.ejecutar() }
                },
                enabled = !cerrandoSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (cerrandoSesion) "Cerrando sesión…" else "Cerrar sesión")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Variante de la pantalla de perfil cuando no hay identificador de usuario.
 * Muestra únicamente el correo y el cierre de sesión.
 */
@Composable
private fun PerfilSinUsuario(
    correoSesion: String?,
    cerrandoSesion: Boolean,
    onCerrarSesion: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp))
            TarjetaDatosUsuario(
                correoSesion = correoSesion,
                nombreUsuario = null,
                nombreEditado = "",
                onNombreCambiado = {},
                aliasEditado = "",
                onAliasCambiado = {}
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onCerrarSesion,
                enabled = !cerrandoSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (cerrandoSesion) "Cerrando sesión…" else "Cerrar sesión")
            }
        }
    }
}

/**
 * @brief Tarjeta con el avatar, el nombre editable, el alias editable y el correo.
 * @param correoSesion Correo electrónico de la sesión activa (puede ser null).
 * @param nombreUsuario Nombre real del usuario observado del perfil (puede ser null).
 * @param nombreEditado Texto actual del campo de edición del nombre.
 * @param onNombreCambiado Callback al cambiar el texto del nombre.
 * @param aliasEditado Texto actual del campo de edición del alias.
 * @param onAliasCambiado Callback al cambiar el texto del alias.
 */
@Composable
private fun TarjetaDatosUsuario(
    correoSesion: String?,
    nombreUsuario: String?,
    nombreEditado: String,
    onNombreCambiado: (String) -> Unit,
    aliasEditado: String,
    onAliasCambiado: (String) -> Unit
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
                    .size(56.dp)
                    .background(AzulSecundario, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val inicial = (nombreUsuario ?: "G").trim().firstOrNull()?.uppercase() ?: "G"
                Text(
                    text = inicial,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (nombreEditado.isNotEmpty()) {
                    OutlinedTextField(
                        value = nombreEditado,
                        onValueChange = onNombreCambiado,
                        label = { Text("Nombre y apellidos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = nombreUsuario ?: "Usuario GYM",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = correoSesion ?: "Sesión iniciada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzulSecundario
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = aliasEditado,
                    onValueChange = onAliasCambiado,
                    label = { Text("Alias (nombre de usuario)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * @brief Formulario de objetivos y datos antropométricos del usuario.
 * Muestra campos numéricos (peso, altura, edad) y selectores (sexo, factor de
 * actividad y objetivo) conectados al estado del ViewModel.
 * @param perfil Perfil actual del usuario.
 * @param guardando Indica si hay una operación de guardado en curso.
 * @param onCampoCambiado Callback al modificar un campo.
 * @param onGuardar Callback para guardar los objetivos.
 */
@Composable
private fun FormularioObjetivos(
    perfil: PerfilUsuario,
    guardando: Boolean,
    onCampoCambiado: (CampoPerfil, String) -> Unit,
    onGuardar: () -> Unit
) {
    Column {
        CampoNumerico(
            etiqueta = "Peso objetivo (kg)",
            valor = perfil.pesoObjetivoKg?.toString().orEmpty(),
            onValorCambiado = { onCampoCambiado(CampoPerfil.PESO, it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        CampoNumerico(
            etiqueta = "Altura (cm)",
            valor = perfil.alturaCm?.toString().orEmpty(),
            onValorCambiado = { onCampoCambiado(CampoPerfil.ALTURA, it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        CampoNumerico(
            etiqueta = "Edad (años)",
            valor = perfil.edad?.toString().orEmpty(),
            onValorCambiado = { onCampoCambiado(CampoPerfil.EDAD, it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MenuDesplegable(
            etiqueta = "Sexo",
            opciones = listOf(PerfilUsuario.SEXO_HOMBRE, PerfilUsuario.SEXO_MUJER),
            seleccion = perfil.sexo.orEmpty(),
            onSeleccion = { onCampoCambiado(CampoPerfil.SEXO, it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MenuDesplegable(
            etiqueta = "Factor de actividad",
            opciones = listOf("SEDENTARIO", "LIGERO", "MODERADO", "FUERTE"),
            seleccion = perfil.factorActividad.orEmpty(),
            onSeleccion = { onCampoCambiado(CampoPerfil.FACTOR_ACTIVIDAD, it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MenuDesplegable(
            etiqueta = "Objetivo nutricional",
            opciones = listOf(
                PerfilUsuario.OBJETIVO_VOLUMEN,
                PerfilUsuario.OBJETIVO_DEFINICION,
                PerfilUsuario.OBJETIVO_MANTENIMIENTO
            ),
            seleccion = perfil.objetivo.orEmpty(),
            onSeleccion = { onCampoCambiado(CampoPerfil.OBJETIVO, it) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onGuardar,
            enabled = !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Guardar objetivos")
            }
        }
    }
}

/**
 * @brief Campo de texto numérico con etiqueta para el formulario de objetivos.
 */
@Composable
private fun CampoNumerico(
    etiqueta: String,
    valor: String,
    onValorCambiado: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambiado,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * @brief Menú desplegable (ExposedDropdownMenuBox) para seleccionar entre opciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}