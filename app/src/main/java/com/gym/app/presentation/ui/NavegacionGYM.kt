/**
 * @file NavegacionGYM.kt
 * @brief Configuración de navegación de la aplicación GYM.
 * Establece la navegación condicional por sesión: si el usuario está autenticado
 * muestra el grafo principal con la barra de navegación inferior; si no lo está
 * (o su configuración está pendiente) muestra la pantalla de autenticación; y si
 * la sesión se está restaurando muestra la pantalla de carga.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gym.app.di.ContenedorDependencias
import com.gym.app.domain.model.EstadoSesion
import com.gym.app.presentation.ui.theme.AzulPrimario
import com.gym.app.presentation.viewmodel.AutenticacionViewModel

/**
 * @enum class DestinoGYM
 * @brief Rutas de las pantallas principales de la aplicación.
 * Cada destino incluye su ruta, título y el icono de la navegación inferior.
 */
enum class DestinoGYM(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    INICIO("inicio", "Inicio", Icons.Filled.Home),
    CALENDARIO("calendario", "Calendario", Icons.Filled.CalendarMonth),
    PESO("peso", "Peso", Icons.Filled.MonitorWeight),
    COMUNIDAD("comunidad", "Comunidad", Icons.Filled.Forum),
    PERFIL("perfil", "Perfil", Icons.Filled.Person)
}

/**
 * @brief Contenedor raíz de navegación de la aplicación GYM.
 *
 * Crea un [AutenticacionViewModel] a partir del [ContenedorDependencias] y
 * observa de forma reactiva el [EstadoSesion] para decidir qué contenido mostrar:
 * - [EstadoSesion.CARGANDO]: pantalla de carga (restauración de sesión).
 * - [EstadoSesion.AUTENTICADO]: grafo principal con barra de navegación inferior.
 * - [EstadoSesion.NO_AUTENTICADO] o [EstadoSesion.CONFIGURACION_PENDIENTE]:
 *   pantalla de autenticación.
 *
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
fun NavegacionGYM(contenedor: ContenedorDependencias) {
    val autenticacionViewModel: AutenticacionViewModel =
        viewModel { AutenticacionViewModel(contenedor) }
    val estado by autenticacionViewModel.estado.collectAsStateWithLifecycle()

    when (estado.estadoSesion) {
        EstadoSesion.CARGANDO -> PantallaCargandoGYM()

        EstadoSesion.AUTENTICADO -> ContenidoAutenticado(contenedor = contenedor)

        EstadoSesion.NO_AUTENTICADO,
        EstadoSesion.CONFIGURACION_PENDIENTE -> PantallaAutenticacionGYM(contenedor = contenedor)
    }
}

/**
 * @brief Grafo de navegación principal mostrado cuando la sesión está activa.
 * Establece la barra de navegación inferior y el grafo de rutas entre las
 * pantallas de Inicio, Peso y Perfil, pasando el contenedor de dependencias
 * a cada pantalla para que resuelvan sus repositorios reales.
 * @param contenedor Contenedor de dependencias de la aplicación.
 */
@Composable
private fun ContenidoAutenticado(contenedor: ContenedorDependencias) {
    val controlador = rememberNavController()
    val estadoNavegacion by controlador.currentBackStackEntryAsState()
    val destinoActual = estadoNavegacion?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                DestinoGYM.entries.forEach { destino ->
                    val seleccionado = destinoActual?.hierarchy
                        ?.any { it.route == destino.ruta } == true
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = {
                            controlador.navigate(destino.ruta) {
                                popUpTo(controlador.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destino.icono,
                                contentDescription = destino.titulo
                            )
                        },
                        label = { Text(destino.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AzulPrimario,
                            selectedTextColor = AzulPrimario,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = controlador,
            startDestination = DestinoGYM.INICIO.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(DestinoGYM.INICIO.ruta) {
                PantallaDashboardGYM(contenedor = contenedor)
            }
            composable(DestinoGYM.CALENDARIO.ruta) {
                PantallaCalendarioEntrenosGYM(contenedor = contenedor)
            }
            composable(DestinoGYM.PESO.ruta) {
                PantallaRegistroPesoGYM(contenedor = contenedor)
            }
            composable(DestinoGYM.COMUNIDAD.ruta) {
                PantallaComunidadGYM()
            }
            composable(DestinoGYM.PERFIL.ruta) {
                PantallaPerfilGYM(contenedor = contenedor)
            }
        }
    }
}