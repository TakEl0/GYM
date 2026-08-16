/**
 * @file PantallaAutenticacionGYM.kt
 * @brief Pantalla de autenticación de la aplicación GYM en Jetpack Compose.
 * Ofrece dos modos conmutables (iniciar sesión y registro) con campos de correo,
 * contraseña y nombre, indicador de carga y manejo de errores visible, todo ello
 * coherente con el tema oscuro azulado de la aplicación (TemaGYM).
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.app.di.ContenedorDependencias
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.ui.theme.AzulSecundario
import com.gym.app.presentation.ui.theme.BlancoAzulado
import com.gym.app.presentation.ui.theme.GrisAzulado
import com.gym.app.presentation.ui.theme.SuperficieElevada
import com.gym.app.presentation.ui.theme.SuperficieOscura
import com.gym.app.presentation.viewmodel.AutenticacionViewModel
import com.gym.app.presentation.viewmodel.EstadoAutenticacion
import com.gym.app.presentation.viewmodel.ModoAutenticacion

/**
 * @brief Pantalla de autenticación de GYM.
 * Crea el [AutenticacionViewModel] a partir del [ContenedorDependencias] y
 * muestra el formulario correspondiente al modo activo (inicio de sesión o
 * registro), junto con el estado reactivo de carga y errores.
 * @param contenedor Contenedor de dependencias de la aplicación.
 * @param viewModelInyectado ViewModel opcional para pruebas de interfaz
 * (si se omite, se crea internamente a partir del contenedor).
 */
@Composable
fun PantallaAutenticacionGYM(
    contenedor: ContenedorDependencias,
    viewModelInyectado: AutenticacionViewModel? = null
) {
    val viewModel: AutenticacionViewModel = viewModelInyectado
        ?: viewModel { AutenticacionViewModel(contenedor) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            CabeceraMarca()
            Spacer(modifier = Modifier.height(32.dp))
            SelectorModo(
                modoActual = estado.modo,
                onModoCambiado = viewModel::cambiarModo
            )
            Spacer(modifier = Modifier.height(24.dp))
            FormularioAutenticacion(
                estado = estado,
                onEmailChanged = viewModel::actualizarEmail,
                onPasswordChanged = viewModel::actualizarPassword,
                onNombreChanged = viewModel::actualizarNombre,
                onAccionPrincipal = {
                    if (estado.modo == ModoAutenticacion.REGISTRO) {
                        viewModel.registrar()
                    } else {
                        viewModel.iniciarSesion()
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * @brief Cabecera con el logotipo textual y el eslogan de la aplicación.
 * Utiliza únicamente iconos vectoriales e texto, sin imágenes externas.
 */
@Composable
private fun CabeceraMarca() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(AzulPrimario, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.FitnessCenter,
            contentDescription = "Logotipo de GYM",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "GYM",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Entrena. Aliméntate. Progresa.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * @brief Selector de modo de la pantalla (inicio de sesión / registro).
 * @param modoActual Modo activo en el estado del ViewModel.
 * @param onModoCambiado Callback invocado al pulsar una de las pestañas.
 */
@Composable
private fun SelectorModo(
    modoActual: ModoAutenticacion,
    onModoCambiado: (ModoAutenticacion) -> Unit
) {
    TabRow(
        selectedTabIndex = indiceDelModo(modoActual),
        containerColor = SuperficieOscura,
        contentColor = AzulPrimario
    ) {
        Tab(
            selected = modoActual == ModoAutenticacion.INICIAR_SESION,
            onClick = { onModoCambiado(ModoAutenticacion.INICIAR_SESION) },
            text = { Text("Iniciar sesión") }
        )
        Tab(
            selected = modoActual == ModoAutenticacion.REGISTRO,
            onClick = { onModoCambiado(ModoAutenticacion.REGISTRO) },
            text = { Text("Registro") }
        )
    }
}

/**
 * @brief Convierte un [ModoAutenticacion] en el índice de pestaña correspondiente.
 * @param modo Modo a convertir.
 * @return 0 para inicio de sesión y 1 para registro.
 */
private fun indiceDelModo(modo: ModoAutenticacion): Int =
    if (modo == ModoAutenticacion.REGISTRO) 1 else 0

/**
 * @brief Formulario de autenticación con campos, errores y botón principal.
 * @param estado Estado inmutable de la autenticación.
 * @param onEmailChanged Callback al editar el campo de correo.
 * @param onPasswordChanged Callback al editar el campo de contraseña.
 * @param onNombreChanged Callback al editar el campo de nombre.
 * @param onAccionPrincipal Callback al pulsar el botón principal.
 */
@Composable
private fun FormularioAutenticacion(
    estado: EstadoAutenticacion,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onNombreChanged: (String) -> Unit,
    onAccionPrincipal: () -> Unit
) {
    OutlinedTextField(
        value = estado.email,
        onValueChange = onEmailChanged,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campoEmail"),
        label = { Text("Correo electrónico") },
        placeholder = { Text("usuario@ejemplo.com") },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.AlternateEmail,
                contentDescription = null,
                tint = AzulSecundario
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        shape = RoundedCornerShape(12.dp),
        colors = coloresCampoTexto()
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (estado.modo == ModoAutenticacion.REGISTRO) {
        OutlinedTextField(
            value = estado.nombre,
            onValueChange = onNombreChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("campoNombre"),
            label = { Text("Nombre completo") },
            placeholder = { Text("Ej. Alex García") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = AzulSecundario
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampoTexto()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    OutlinedTextField(
        value = estado.password,
        onValueChange = onPasswordChanged,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campoPassword"),
        label = { Text("Contraseña") },
        placeholder = { Text("Mínimo 8 caracteres") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = AzulSecundario
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        colors = coloresCampoTexto()
    )
    Spacer(modifier = Modifier.height(16.dp))

    estado.error?.let { mensajeError ->
        Text(
            text = mensajeError,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (estado.exitoLogin) {
        Text(
            text = "Operación completada correctamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = AzulSecundario,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    val esRegistro = estado.modo == ModoAutenticacion.REGISTRO
    Button(
        onClick = onAccionPrincipal,
        enabled = !estado.cargando,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (estado.cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = if (esRegistro) "Registrarme" else "Iniciar sesión",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * @brief Devuelve los colores personalizados de los campos de texto.
 * Ajusta el tema oscuro azulado a los campos de entrada del formulario,
 * replicando el estilo visual del resto de pantallas de la aplicación.
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
        cursorColor = AzulPrimario,
        focusedContainerColor = SuperficieOscura,
        unfocusedContainerColor = SuperficieOscura
    )