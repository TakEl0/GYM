/**
 * @file NavegacionGYM.kt
 * @brief Configuración de navegación de la aplicación GYM.
 * Define las rutas de las pantallas principales y el grafo de navegación
 * con la navegación inferior entre las secciones de la aplicación.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gym.app.presentation.ui.theme.AzulPrimario

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
    PESO("peso", "Peso", Icons.Filled.MonitorWeight)
}

/**
 * @brief Contenedor raíz de navegación de la aplicación GYM.
 * Establece la navegación inferior y el grafo de rutas entre las pantallas.
 */
@Composable
fun NavegacionGYM() {
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
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
                PantallaDashboardGYM()
            }
            composable(DestinoGYM.PESO.ruta) {
                PantallaRegistroPesoGYM()
            }
        }
    }
}