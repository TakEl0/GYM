/**
 * @file DashboardViewModel.kt
 * @brief ViewModel del panel de control (Dashboard) de la aplicación GYM.
 * Expone el estado inmutable del panel de control mediante StateFlow y
 * delega la obtención de datos en el repositorio de entrenamientos.
 */
package com.gym.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.app.data.repository.RepositorioEntrenamientoFake
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.repository.RepositorioEntrenamiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @data class EstadoDashboard
 * @brief Estado inmutable del panel de control.
 * @property entrenamientoDeHoy Rutina programada para hoy (puede ser null).
 * @property sesionesCompletadas Semanas completadas esta semana.
 * @property totalSesionesSemana Sesiones totales planificadas en la semana.
 * @property cargando Indica si los datos están en proceso de carga.
 * @property error Mensaje de error si la carga falló (null en caso normal).
 */
data class EstadoDashboard(
    val entrenamientoDeHoy: Entrenamiento? = null,
    val sesionesCompletadas: Int = 0,
    val totalSesionesSemana: Int = 0,
    val cargando: Boolean = false,
    val error: String? = null
)

/**
 * @class DashboardViewModel
 * @brief Gestiona el estado del panel de control principal.
 * Inyecta el repositorio de entrenamientos para obtener la rutina del día
 * y el resumen semanal, actualizando el StateFlow de forma reactiva.
 * Si no se proporciona un repositorio, se utiliza la implementación simulada
 * de la capa de datos para facilitar el desarrollo.
 */
class DashboardViewModel(
    private val repositorioEntrenamiento: RepositorioEntrenamiento = RepositorioEntrenamientoFake()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoDashboard())
    val estado: StateFlow<EstadoDashboard> = _estado.asStateFlow()

    /**
     * @brief Inicializa la carga de los datos del panel de control.
     * Recupera la rutina de hoy y los contadores semanales desde el repositorio.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val entrenamiento = repositorioEntrenamiento.obtenerEntrenamientoDeHoy()
                val completadas = repositorioEntrenamiento.obtenerSesionesCompletadasSemana()
                val total = repositorioEntrenamiento.obtenerTotalSesionesSemana()
                _estado.update {
                    it.copy(
                        entrenamientoDeHoy = entrenamiento,
                        sesionesCompletadas = completadas,
                        totalSesionesSemana = total,
                        cargando = false
                    )
                }
            } catch (excepcion: Exception) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar los datos del entrenamiento."
                    )
                }
            }
        }
    }
}